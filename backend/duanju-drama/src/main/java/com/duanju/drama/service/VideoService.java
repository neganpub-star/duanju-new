package com.duanju.drama.service;

import com.duanju.common.core.page.PageQuery;
import com.duanju.common.core.page.PageResult;
import com.duanju.drama.domain.Video;

import java.util.List;

public interface VideoService {

    PageResult<Video> pageList(PageQuery pageQuery, Integer siteId, String keyword, Long categoryId, Integer status);

    Video getDetail(Long videoId);

    List<Video> recommend(Integer siteId, Integer limit);

    void addViewCount(Long videoId);

    boolean saveVideo(Video video);

    boolean updateVideo(Video video);

    boolean removeVideo(Long videoId);
}
