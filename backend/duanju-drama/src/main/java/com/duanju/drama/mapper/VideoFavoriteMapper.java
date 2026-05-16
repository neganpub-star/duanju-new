package com.duanju.drama.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duanju.drama.domain.VideoFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface VideoFavoriteMapper extends BaseMapper<VideoFavorite> {

    @Select("SELECT vf.video_id as videoId, v.title, v.cover, v.image, v.description, v.series_count as seriesCount " +
            "FROM vs_drama_video_favorite vf " +
            "JOIN vs_drama_video v ON v.id = vf.video_id AND v.delete_time IS NULL " +
            "WHERE vf.user_id = #{userId} AND vf.delete_time IS NULL " +
            "ORDER BY vf.create_time DESC " +
            "LIMIT #{offset}, #{limit}")
    List<Map<String, Object>> selectFavoriteList(@Param("userId") Long userId,
                                                  @Param("offset") int offset,
                                                  @Param("limit") int limit);
}
