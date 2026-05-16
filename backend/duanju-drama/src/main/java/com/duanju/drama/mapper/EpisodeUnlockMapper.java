package com.duanju.drama.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duanju.drama.domain.EpisodeUnlock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EpisodeUnlockMapper extends BaseMapper<EpisodeUnlock> {

    @Select("SELECT COUNT(1) FROM vs_drama_episode_unlock WHERE user_id=#{userId} AND episode_id=#{episodeId} AND delete_time IS NULL")
    int existsUnlock(@Param("userId") Long userId, @Param("episodeId") Long episodeId);
}
