package com.duanju.drama.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duanju.drama.domain.CommentLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Set;

@Mapper
public interface CommentLikeMapper extends BaseMapper<CommentLike> {

    @Select("<script>" +
            "SELECT comment_id FROM vs_drama_comment_like " +
            "WHERE user_id = #{userId} AND delete_time IS NULL " +
            "AND comment_id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    List<Long> selectLikedCommentIds(@Param("userId") Long userId, @Param("ids") List<Long> ids);
}
