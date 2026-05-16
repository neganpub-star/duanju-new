package com.duanju.drama.service.impl;

import com.duanju.drama.domain.VideoEpisodes;
import com.duanju.drama.mapper.VideoEpisodesMapper;
import com.duanju.drama.service.TranscodeService;
import com.duanju.system.service.SysConfigService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service("ffmpeg")
@RequiredArgsConstructor
public class FfmpegTranscodeService implements TranscodeService {

    private final SysConfigService configService;
    private final VideoEpisodesMapper episodesMapper;
    private final ObjectMapper objectMapper;

    /** 默认清晰度配置 */
    private static final String DEFAULT_QUALITIES =
        "[{\"definition\":\"360p\",\"scale\":\"640:360\",\"bitrate\":\"800k\"}," +
        "{\"definition\":\"720p\",\"scale\":\"1280:720\",\"bitrate\":\"2000k\"}," +
        "{\"definition\":\"1080p\",\"scale\":\"1920:1080\",\"bitrate\":\"4000k\"}]";

    @Override
    @Async
    public void submit(Long episodeId, String sourceUrl) {
        log.info("开始转码 episodeId={}, source={}", episodeId, sourceUrl);
        updateStatus(episodeId, "processing", null);

        // 每次转码实时从 DB 读取配置，管理员改完即生效
        String bin       = configService.getValue("transcode.ffmpeg.bin",               "ffmpeg");
        String outputDir = configService.getValue("transcode.ffmpeg.output-dir",        "/data/videos");
        String urlPrefix = configService.getValue("transcode.ffmpeg.output-url-prefix", "http://localhost/videos");
        String qualJson  = configService.getValue("transcode.ffmpeg.qualities",         DEFAULT_QUALITIES);

        List<Quality> qualities;
        try {
            qualities = objectMapper.readValue(qualJson, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("解析清晰度配置失败，使用默认配置", e);
            try {
                qualities = objectMapper.readValue(DEFAULT_QUALITIES, new TypeReference<>() {});
            } catch (Exception ex) {
                updateStatus(episodeId, "failed", "清晰度配置解析失败");
                return;
            }
        }

        List<String> playInfoItems = new ArrayList<>();

        try {
            for (Quality q : qualities) {
                String outDir = outputDir + "/" + episodeId + "/" + q.getDefinition();
                new File(outDir).mkdirs();

                List<String> cmd = List.of(
                    bin,
                    "-i", sourceUrl,
                    "-c:v", "libx264",
                    "-b:v", q.getBitrate(),
                    "-vf", "scale=" + q.getScale(),
                    "-c:a", "aac",
                    "-hls_time", "10",
                    "-hls_list_size", "0",
                    "-hls_segment_filename", outDir + "/seg_%03d.ts",
                    "-f", "hls",
                    outDir + "/index.m3u8",
                    "-y"
                );

                log.info("转码 [{}]: {}", q.getDefinition(), String.join(" ", cmd));
                Process process = new ProcessBuilder(cmd)
                    .redirectErrorStream(true)
                    .start();

                // 消费进程输出，避免缓冲区满导致进程阻塞
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        log.debug("ffmpeg [{}]: {}", q.getDefinition(), line);
                    }
                }

                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    throw new RuntimeException("FFmpeg 退出码=" + exitCode + "，清晰度=" + q.getDefinition());
                }

                String playUrl = urlPrefix + "/" + episodeId + "/" + q.getDefinition() + "/index.m3u8";
                playInfoItems.add(String.format("{\"definition\":\"%s\",\"url\":\"%s\"}", q.getDefinition(), playUrl));
                log.info("转码完成 [{}]", q.getDefinition());
            }

            String playInfoJson = "[" + String.join(",", playInfoItems) + "]";
            VideoEpisodes update = new VideoEpisodes();
            update.setId(episodeId);
            update.setPlayInfo(playInfoJson);
            update.setTranscodeStatus("done");
            episodesMapper.updateById(update);
            log.info("全部转码完成 episodeId={}", episodeId);

        } catch (Exception e) {
            log.error("转码失败 episodeId={}", episodeId, e);
            updateStatus(episodeId, "failed", e.getMessage());
        }
    }

    private void updateStatus(Long episodeId, String status, String msg) {
        VideoEpisodes update = new VideoEpisodes();
        update.setId(episodeId);
        update.setTranscodeStatus(status);
        update.setTranscodeMsg(msg);
        episodesMapper.updateById(update);
    }

    @Data
    static class Quality {
        private String definition;
        private String scale;
        private String bitrate;
    }
}
