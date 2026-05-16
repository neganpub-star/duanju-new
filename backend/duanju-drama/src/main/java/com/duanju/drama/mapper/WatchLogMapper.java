package com.duanju.drama.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duanju.drama.domain.WatchLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface WatchLogMapper extends BaseMapper<WatchLog> {

    @Select("SELECT wl.video_id as videoId, wl.episode_id as episodeId, wl.view_time as viewTime, " +
            "v.title, v.title_i18n as titleI18n, v.cover, v.image, " +
            "v.description, v.desc_i18n as descI18n, v.series_count as seriesCount, " +
            "e.title as episodeName, e.title_i18n as episodeTitleI18n " +
            "FROM vs_drama_watch_log wl " +
            "JOIN vs_drama_video v ON v.id = wl.video_id AND v.delete_time IS NULL " +
            "LEFT JOIN vs_drama_video_episodes e ON e.id = wl.episode_id AND e.delete_time IS NULL " +
            "WHERE wl.user_id = #{userId} AND wl.delete_time IS NULL " +
            "ORDER BY wl.update_time DESC " +
            "LIMIT #{offset}, #{limit}")
    List<Map<String, Object>> selectWatchLogList(@Param("userId") Long userId,
                                                  @Param("offset") int offset,
                                                  @Param("limit") int limit);
}
