package com.duanju.drama.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duanju.common.core.page.PageQuery;
import com.duanju.common.core.page.PageResult;
import com.duanju.common.exception.ServiceException;
import com.duanju.drama.domain.Video;
import com.duanju.drama.mapper.VideoMapper;
import com.duanju.drama.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video> implements VideoService {

    @Override
    public PageResult<Video> pageList(PageQuery pageQuery, Integer siteId, String keyword, Long categoryId, Integer status) {
        Page<Video> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        baseMapper.selectPageByCond(page, siteId, keyword, categoryId, status);
        return PageResult.of(page);
    }

    @Override
    public Video getDetail(Long videoId) {
        Video video = baseMapper.selectById(videoId);
        if (video == null) {
            throw ServiceException.notFound("短剧不存在");
        }
        return video;
    }

    @Override
    public List<Video> recommend(Integer siteId, Integer limit) {
        Page<Video> page = new Page<>(1, limit);
        baseMapper.selectPageByCond(page, siteId, null, null, 1);
        return page.getRecords();
    }

    @Override
    public void addViewCount(Long videoId) {
        Video video = new Video();
        video.setId(videoId);
        video.setViews((baseMapper.selectById(videoId).getViews()) + 1);
        baseMapper.updateById(video);
    }

    @Override
    public boolean saveVideo(Video video) {
        return save(video);
    }

    @Override
    public boolean updateVideo(Video video) {
        return updateById(video);
    }

    @Override
    public boolean removeVideo(Long videoId) {
        return removeById(videoId);
    }
}
