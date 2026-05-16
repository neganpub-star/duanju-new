package com.duanju.api.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.duanju.common.core.domain.R;
import com.duanju.drama.domain.Comment;
import com.duanju.drama.domain.CommentLike;
import com.duanju.drama.mapper.CommentLikeMapper;
import com.duanju.drama.mapper.CommentMapper;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "评论接口")
@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentMapper commentMapper;
    private final CommentLikeMapper commentLikeMapper;

    @Value("${duanju.site-id:1}")
    private Integer siteId;

    @Operation(summary = "评论列表（顶级）")
    @GetMapping("/list")
    public R<Map<String, Object>> list(
            @RequestParam Long videoId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pagesize) {

        int offset = (page - 1) * pagesize;
        List<Map<String, Object>> rows = commentMapper.selectTopComments(videoId, offset, pagesize);
        int total = commentMapper.countTopComments(videoId);

        // 叠加当前用户的点赞状态
        enrichLikeStatus(rows);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", total);
        result.put("list", rows);
        return R.ok(result);
    }

    @Operation(summary = "回复列表")
    @GetMapping("/replies")
    public R<List<Map<String, Object>>> replies(
            @RequestParam Long parentId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pagesize) {

        int offset = (page - 1) * pagesize;
        List<Map<String, Object>> rows = commentMapper.selectReplies(parentId, offset, pagesize);
        enrichLikeStatus(rows);
        return R.ok(rows);
    }

    @Operation(summary = "发表评论 / 回复")
    @PostMapping("/post")
    public R<Map<String, Object>> post(@RequestBody CommentReq req) {
        if (!StpUtil.isLogin()) return R.fail("请先登录");
        if (req.getContent() == null || req.getContent().trim().isEmpty()) return R.fail("评论内容不能为空");
        if (req.getContent().length() > 500) return R.fail("评论内容不能超过500字");

        long userId = StpUtil.getLoginIdAsLong();
        Comment comment = new Comment();
        comment.setSiteId(siteId);
        comment.setUserId(userId);
        comment.setVideoId(req.getVideoId());
        comment.setParentId(req.getParentId() != null ? req.getParentId() : 0L);
        comment.setReplyUserId(req.getReplyUserId() != null ? req.getReplyUserId() : 0L);
        comment.setReplyNickname(req.getReplyNickname() != null ? req.getReplyNickname() : "");
        comment.setContent(req.getContent().trim());
        comment.setLikes(0);
        comment.setStatus(1);
        commentMapper.insert(comment);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("id", comment.getId());
        return R.ok(resp);
    }

    @Operation(summary = "点赞 / 取消点赞评论")
    @PostMapping("/like")
    public R<Map<String, Object>> like(@RequestBody LikeReq req) {
        if (!StpUtil.isLogin()) return R.fail("请先登录");
        long userId = StpUtil.getLoginIdAsLong();

        CommentLike existing = commentLikeMapper.selectOne(new LambdaQueryWrapper<CommentLike>()
                .eq(CommentLike::getUserId, userId)
                .eq(CommentLike::getCommentId, req.getCommentId())
                .isNull(CommentLike::getDeleteTime));

        boolean liked;
        if (existing == null) {
            // 点赞
            CommentLike like = new CommentLike();
            like.setUserId(userId);
            like.setCommentId(req.getCommentId());
            commentLikeMapper.insert(like);
            commentMapper.updateLikes(req.getCommentId(), 1);
            liked = true;
        } else {
            // 取消点赞（软删除）
            commentLikeMapper.deleteById(existing.getId());
            commentMapper.updateLikes(req.getCommentId(), -1);
            liked = false;
        }

        Comment c = commentMapper.selectById(req.getCommentId());
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("liked", liked);
        resp.put("likes", c != null ? c.getLikes() : 0);
        return R.ok(resp);
    }

    @Operation(summary = "删除自己的评论")
    @PostMapping("/delete")
    public R<Void> delete(@RequestBody DeleteReq req) {
        if (!StpUtil.isLogin()) return R.fail("请先登录");
        long userId = StpUtil.getLoginIdAsLong();

        Comment comment = commentMapper.selectById(req.getCommentId());
        if (comment == null || comment.getDeleteTime() != null) return R.fail("评论不存在");
        if (!comment.getUserId().equals(userId)) return R.fail("无权删除");

        commentMapper.deleteById(req.getCommentId());
        // 同步删除该评论下的所有回复
        commentMapper.delete(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getParentId, req.getCommentId()));
        return R.ok();
    }

    private void enrichLikeStatus(List<Map<String, Object>> rows) {
        if (rows.isEmpty() || !StpUtil.isLogin()) {
            rows.forEach(r -> r.put("isLiked", false));
            return;
        }
        long userId = StpUtil.getLoginIdAsLong();
        List<Long> ids = rows.stream()
                .map(r -> Long.parseLong(r.get("id").toString()))
                .collect(Collectors.toList());
        Set<Long> likedSet = new HashSet<>(commentLikeMapper.selectLikedCommentIds(userId, ids));
        rows.forEach(r -> {
            Long id = Long.parseLong(r.get("id").toString());
            r.put("isLiked", likedSet.contains(id));
            // 确保 isOwn 字段存在
            r.put("isOwn", userId == Long.parseLong(r.get("userId").toString()));
        });
    }

    @Data
    public static class CommentReq {
        @JsonProperty("video_id")
        private Long videoId;
        private String content;
        @JsonProperty("parent_id")
        private Long parentId;
        @JsonProperty("reply_user_id")
        private Long replyUserId;
        @JsonProperty("reply_nickname")
        private String replyNickname;
    }

    @Data
    public static class LikeReq {
        @JsonProperty("comment_id")
        private Long commentId;
    }

    @Data
    public static class DeleteReq {
        @JsonProperty("comment_id")
        private Long commentId;
    }
}
