package com.duanju.drama.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duanju.drama.domain.VideoEpisodes;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface VideoEpisodesMapper extends BaseMapper<VideoEpisodes> {

    @Select("SELECT * FROM vs_drama_video_episodes WHERE video_id = #{videoId} AND delete_time IS NULL ORDER BY episode_num ASC")
    List<VideoEpisodes> selectByVideoId(@Param("videoId") Long videoId);
}
