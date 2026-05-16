package com.duanju.drama.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duanju.drama.domain.VideoLike;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface VideoLikeMapper extends BaseMapper<VideoLike> {

    @Select("SELECT COUNT(1) FROM vs_drama_video_like WHERE user_id = #{userId} AND video_id = #{videoId}")
    int existsByUserAndVideo(@Param("userId") long userId, @Param("videoId") long videoId);

    @Delete("DELETE FROM vs_drama_video_like WHERE user_id = #{userId} AND video_id = #{videoId}")
    int deleteByUserAndVideo(@Param("userId") long userId, @Param("videoId") long videoId);
}
