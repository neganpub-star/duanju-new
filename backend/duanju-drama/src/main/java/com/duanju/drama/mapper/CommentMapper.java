package com.duanju.drama.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duanju.drama.domain.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    @Select("SELECT c.id, c.user_id as userId, c.video_id as videoId, c.parent_id as parentId, " +
            "c.reply_user_id as replyUserId, c.reply_nickname as replyNickname, " +
            "c.content, c.likes, c.create_time as createTime, " +
            "u.nickname as userNickname, u.avatar as userAvatar, " +
            "(SELECT COUNT(*) FROM vs_drama_comment r WHERE r.parent_id = c.id AND r.delete_time IS NULL) as replyCount " +
            "FROM vs_drama_comment c " +
            "JOIN vs_drama_user u ON u.id = c.user_id " +
            "WHERE c.video_id = #{videoId} AND c.parent_id = 0 AND c.delete_time IS NULL AND c.status = 1 " +
            "ORDER BY c.create_time DESC " +
            "LIMIT #{offset}, #{limit}")
    List<Map<String, Object>> selectTopComments(@Param("videoId") Long videoId,
                                                 @Param("offset") int offset,
                                                 @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM vs_drama_comment WHERE video_id = #{videoId} AND parent_id = 0 AND delete_time IS NULL AND status = 1")
    int countTopComments(@Param("videoId") Long videoId);

    @Select("SELECT c.id, c.user_id as userId, c.parent_id as parentId, " +
            "c.reply_user_id as replyUserId, c.reply_nickname as replyNickname, " +
            "c.content, c.likes, c.create_time as createTime, " +
            "u.nickname as userNickname, u.avatar as userAvatar " +
            "FROM vs_drama_comment c " +
            "JOIN vs_drama_user u ON u.id = c.user_id " +
            "WHERE c.parent_id = #{parentId} AND c.delete_time IS NULL AND c.status = 1 " +
            "ORDER BY c.create_time ASC " +
            "LIMIT #{offset}, #{limit}")
    List<Map<String, Object>> selectReplies(@Param("parentId") Long parentId,
                                             @Param("offset") int offset,
                                             @Param("limit") int limit);

    @Update("UPDATE vs_drama_comment SET likes = likes + #{delta} WHERE id = #{id}")
    void updateLikes(@Param("id") Long id, @Param("delta") int delta);
}
