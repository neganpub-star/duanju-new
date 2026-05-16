package com.duanju.admin.controller.drama;

import com.duanju.common.core.domain.R;
import com.duanju.common.core.page.PageQuery;
import com.duanju.common.core.page.PageResult;
import com.duanju.drama.domain.Video;
import com.duanju.drama.domain.VideoEpisodes;
import com.duanju.drama.mapper.VideoEpisodesMapper;
import com.duanju.drama.service.TranscodeService;
import com.duanju.drama.service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "【后管】短剧管理")
@RestController
@RequestMapping("/admin/drama/video")
@RequiredArgsConstructor
public class AdminVideoController {

    private final VideoService videoService;
    private final VideoEpisodesMapper episodesMapper;
    private final TranscodeService transcodeService;

    @Operation(summary = "短剧列表")
    @GetMapping("/list")
    public R<PageResult<Video>> list(PageQuery pageQuery,
                                     @RequestParam(required = false) String keyword,
                                     @RequestParam(required = false) Long categoryId,
                                     @RequestParam(required = false) Integer status,
                                     @RequestParam(defaultValue = "1") Integer siteId) {
        return R.ok(videoService.pageList(pageQuery, siteId, keyword, categoryId, status));
    }

    @Operation(summary = "短剧详情")
    @GetMapping("/{id}")
    public R<Video> detail(@PathVariable Long id) {
        return R.ok(videoService.getDetail(id));
    }

    @Operation(summary = "新增短剧")
    @PostMapping
    public R<Void> add(@Valid @RequestBody Video video) {
        videoService.saveVideo(video);
        return R.ok();
    }

    @Operation(summary = "修改短剧")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody Video video) {
        video.setId(id);
        videoService.updateVideo(video);
        return R.ok();
    }

    @Operation(summary = "删除短剧")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        videoService.removeVideo(id);
        return R.ok();
    }

    @Operation(summary = "剧集列表")
    @GetMapping("/{videoId}/episodes")
    public R<List<VideoEpisodes>> episodes(@PathVariable Long videoId) {
        return R.ok(episodesMapper.selectByVideoId(videoId));
    }

    @Operation(summary = "新增剧集")
    @PostMapping("/{videoId}/episodes")
    public R<Void> addEpisode(@PathVariable Long videoId, @RequestBody VideoEpisodes ep) {
        ep.setVideoId(videoId);
        if (ep.getStatus() == null) ep.setStatus(1);
        if (ep.getIsFree() == null) ep.setIsFree(0);
        episodesMapper.insert(ep);
        return R.ok();
    }

    @Operation(summary = "修改剧集")
    @PutMapping("/episodes/{epId}")
    public R<Void> updateEpisode(@PathVariable Long epId, @RequestBody VideoEpisodes ep) {
        ep.setId(epId);
        episodesMapper.updateById(ep);
        return R.ok();
    }

    @Operation(summary = "删除剧集")
    @DeleteMapping("/episodes/{epId}")
    public R<Void> deleteEpisode(@PathVariable Long epId) {
        VideoEpisodes ep = new VideoEpisodes();
        ep.setId(epId);
        ep.setDeleteTime(java.time.LocalDateTime.now());
        episodesMapper.updateById(ep);
        return R.ok();
    }

    @Operation(summary = "触发分集转码")
    @PostMapping("/episodes/{epId}/transcode")
    public R<Void> transcode(@PathVariable Long epId) {
        VideoEpisodes ep = episodesMapper.selectById(epId);
        if (ep == null) return R.fail("分集不存在");
        if (!StringUtils.hasText(ep.getUrl())) return R.fail("请先设置视频地址");
        if ("processing".equals(ep.getTranscodeStatus())) return R.fail("正在转码中，请勿重复提交");
        // 先标记 pending，再异步执行
        VideoEpisodes mark = new VideoEpisodes();
        mark.setId(epId);
        mark.setTranscodeStatus("pending");
        mark.setTranscodeMsg(null);
        episodesMapper.updateById(mark);
        transcodeService.submit(epId, ep.getUrl());
        return R.ok();
    }

    @Operation(summary = "批量排序剧集")
    @PutMapping("/{videoId}/episodes/sort")
    public R<Void> sortEpisodes(@PathVariable Long videoId, @RequestBody List<VideoEpisodes> episodes) {
        episodes.forEach(ep -> {
            VideoEpisodes update = new VideoEpisodes();
            update.setId(ep.getId());
            update.setWeigh(ep.getWeigh());
            update.setEpisodeNum(ep.getEpisodeNum());
            episodesMapper.updateById(update);
        });
        return R.ok();
    }
}
