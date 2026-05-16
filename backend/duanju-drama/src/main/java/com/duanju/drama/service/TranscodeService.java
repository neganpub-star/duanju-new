package com.duanju.drama.service;

public interface TranscodeService {

    /**
     * 异步提交转码任务
     *
     * @param episodeId  分集ID
     * @param sourceUrl  原始视频地址
     */
    void submit(Long episodeId, String sourceUrl);
}
