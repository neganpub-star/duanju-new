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
 * 阿里云 VOD 转码服务（待完整实现）
 *
 * 接入步骤：
 *   1. 引入 aliyun-java-sdk-vod 依赖
 *   2. CreateUploadVideo → 获取 uploadAuth / uploadAddress / videoId
 *   3. VOD 上传 SDK 将 sourceUrl 文件上传至 OSS
 *   4. 等待转码完成（MNS 回调 或 轮询 GetVideoInfo）
 *   5. GetPlayInfo → 解析各清晰度 URL → 写入分集 play_info 字段
 */
@Slf4j
@Service("aliyun")
@RequiredArgsConstructor
public class AliyunVodTranscodeService implements TranscodeService {

    // 系统参数配置键
    private static final String KEY_ACCESS_KEY_ID     = "transcode.aliyun.access-key-id";
    private static final String KEY_ACCESS_KEY_SECRET = "transcode.aliyun.access-key-secret";
    private static final String KEY_REGION_ID         = "transcode.aliyun.region-id";
    private static final String KEY_TEMPLATE_GROUP_ID = "transcode.aliyun.template-group-id";

    private final SysConfigService configService;
    private final VideoEpisodesMapper episodesMapper;

    @Override
    @Async
    public void submit(Long episodeId, String sourceUrl) {
        // 读取阿里云 VOD 配置
        String accessKeyId     = configService.getValue(KEY_ACCESS_KEY_ID,     "");
        String accessKeySecret = configService.getValue(KEY_ACCESS_KEY_SECRET, "");
        String regionId        = configService.getValue(KEY_REGION_ID,         "cn-shanghai");
        String templateGroupId = configService.getValue(KEY_TEMPLATE_GROUP_ID, "");

        log.info("阿里云 VOD 转码提交 episodeId={}, region={}, templateGroup={}",
                episodeId, regionId, templateGroupId);

        // 前置校验：必要参数不能为空
        if (accessKeyId.isEmpty() || accessKeySecret.isEmpty()) {
            fail(episodeId, "未配置阿里云 AccessKey，请在参数配置中填写");
            return;
        }
        if (templateGroupId.isEmpty()) {
            fail(episodeId, "未配置转码模板组 ID，请在参数配置中填写");
            return;
        }

        // TODO: 调用阿里云 VOD SDK 完成转码，参见类注释中的接入步骤
        fail(episodeId, "阿里云 VOD 转码功能待实现，请参考类注释补充 SDK 调用代码");
        log.warn("阿里云 VOD 转码服务尚未完整实现 episodeId={}", episodeId);
    }

    // 标记转码失败并记录原因
    private void fail(Long episodeId, String msg) {
        updateStatus(episodeId, "failed", msg);
    }

    // 更新分集的转码状态
    private void updateStatus(Long episodeId, String status, String msg) {
        VideoEpisodes update = new VideoEpisodes();
        update.setId(episodeId);
        update.setTranscodeStatus(status);
        update.setTranscodeMsg(msg);
        episodesMapper.updateById(update);
    }
}
