package com.duanju.drama.service.impl;

import com.duanju.drama.domain.VideoEpisodes;
import com.duanju.drama.mapper.VideoEpisodesMapper;
import com.duanju.drama.service.TranscodeService;
import com.duanju.system.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 阿里云 VOD 转码服务
 *
 * 流程说明：
 *   1. 调 CreateUploadVideo 获取上传凭证和 VideoId
 *   2. 使用凭证将视频上传到阿里云 OSS
 *   3. VOD 根据转码模板组自动触发转码
 *   4. 通过 MNS 回调或轮询 GetVideoInfo 等待转码完成
 *   5. 调 GetPlayInfo 获取各清晰度播放地址，写入 play_info 字段
 *
 * 注意：完整实现需引入 aliyun-java-sdk-vod 依赖并实现回调接口。
 */
@Slf4j
@Service("aliyun")
@RequiredArgsConstructor
public class AliyunVodTranscodeService implements TranscodeService {

    private final SysConfigService configService;
    private final VideoEpisodesMapper episodesMapper;

    @Override
    @Async
    public void submit(Long episodeId, String sourceUrl) {
        String accessKeyId      = configService.getValue("transcode.aliyun.access-key-id",      "");
        String accessKeySecret  = configService.getValue("transcode.aliyun.access-key-secret",  "");
        String regionId         = configService.getValue("transcode.aliyun.region-id",          "cn-shanghai");
        String templateGroupId  = configService.getValue("transcode.aliyun.template-group-id",  "");

        log.info("阿里云 VOD 转码 episodeId={}, regionId={}, templateGroupId={}",
            episodeId, regionId, templateGroupId);

        if (accessKeyId.isEmpty() || accessKeySecret.isEmpty()) {
            updateStatus(episodeId, "failed", "阿里云 VOD 未配置 AccessKey，请在参数配置中填写");
            return;
        }
        if (templateGroupId.isEmpty()) {
            updateStatus(episodeId, "failed", "阿里云 VOD 未配置转码模板组 ID，请在参数配置中填写");
            return;
        }

        // TODO: 接入 aliyun-java-sdk-vod，实现以下步骤：
        //   1. new DefaultAcsClient(region, accessKeyId, accessKeySecret)
        //   2. CreateUploadVideoRequest → 获取 uploadAuth / uploadAddress / videoId
        //   3. 使用 VOD 上传 SDK 将 sourceUrl 对应的文件上传到 OSS
        //   4. 等待 VOD 转码完成（回调或轮询 GetVideoInfo）
        //   5. GetPlayInfoRequest(videoId) → 解析各清晰度 URL → 存入 play_info
        updateStatus(episodeId, "failed", "阿里云 VOD 转码功能待实现，请参考注释补充 SDK 调用代码");
        log.warn("阿里云 VOD 转码服务尚未完整实现 episodeId={}", episodeId);
    }

    private void updateStatus(Long episodeId, String status, String msg) {
        VideoEpisodes update = new VideoEpisodes();
        update.setId(episodeId);
        update.setTranscodeStatus(status);
        update.setTranscodeMsg(msg);
        episodesMapper.updateById(update);
    }
}
