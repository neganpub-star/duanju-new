package com.duanju.drama.service.impl;

import com.duanju.drama.service.TranscodeService;
import com.duanju.system.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * 转码路由：根据参数配置中的 transcode.provider 选择对应的转码实现。
 * 目前支持：ffmpeg（本地转码）、aliyun（阿里云 VOD）
 */
@Slf4j
@Primary
@Service
@RequiredArgsConstructor
public class TranscodeRouter implements TranscodeService {

    private final SysConfigService configService;
    private final ApplicationContext ctx;

    @Override
    public void submit(Long episodeId, String sourceUrl) {
        String provider = configService.getValue("transcode.provider", "ffmpeg");
        log.info("转码路由 episodeId={}, provider={}", episodeId, provider);
        try {
            TranscodeService service = ctx.getBean(provider, TranscodeService.class);
            service.submit(episodeId, sourceUrl);
        } catch (NoSuchBeanDefinitionException e) {
            log.error("未找到转码服务实现: {}，回退到 ffmpeg", provider);
            ctx.getBean("ffmpeg", TranscodeService.class).submit(episodeId, sourceUrl);
        }
    }
}
