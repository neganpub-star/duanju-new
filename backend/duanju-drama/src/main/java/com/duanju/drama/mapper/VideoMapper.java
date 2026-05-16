package com.duanju.drama.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.duanju.drama.domain.Video;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface VideoMapper extends BaseMapper<Video> {

    IPage<Video> selectPageByCond(
            IPage<Video> page,
            @Param("siteId") Integer siteId,
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("status") Integer status
    );

    @Update("UPDATE vs_drama_video SET likes = GREATEST(0, COALESCE(likes,0) + #{delta}) WHERE id = #{id}")
    void updateLikesCount(@Param("id") Long id, @Param("delta") int delta);

    @Update("UPDATE vs_drama_video SET shares = COALESCE(shares,0) + 1 WHERE id = #{id}")
    void incrementSharesCount(@Param("id") Long id);
}
