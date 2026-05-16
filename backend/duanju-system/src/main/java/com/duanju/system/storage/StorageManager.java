package com.duanju.system.storage;

import com.duanju.system.service.SysConfigService;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 存储管理器：根据 DB 配置 storage.provider 动态选择存储实现
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StorageManager {

    private final SysConfigService configService;

    /**
     * 获取当前配置的存储服务实例
     */
    public StorageService getStorageService() {
        String provider = configService.getValue("storage.provider", "alioss");
        log.info("当前存储提供商: {}", provider);
        if ("minio".equals(provider)) {
            return buildMinio();
        }
        return buildAliOss();
    }

    private AliOssStorageService buildAliOss() {
        String endpoint        = configService.getValue("alioss.endpoint", "");
        String accessKeyId     = configService.getValue("alioss.accessKeyId", "");
        String accessKeySecret = configService.getValue("alioss.accessKeySecret", "");
        String bucketName      = configService.getValue("alioss.bucketName", "");
        String accessUrl       = configService.getValue("alioss.accessUrl", "");
        return new AliOssStorageService(endpoint, accessKeyId, accessKeySecret, bucketName, accessUrl);
    }

    private MinioStorageService buildMinio() {
        String endpoint   = configService.getValue("minio.endpoint", "");
        String accessKey  = configService.getValue("minio.accessKey", "");
        String secretKey  = configService.getValue("minio.secretKey", "");
        String bucketName = configService.getValue("minio.bucketName", "");
        String accessUrl  = configService.getValue("minio.accessUrl", "");
        MinioClient client = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        return new MinioStorageService(client, bucketName, accessUrl);
    }
}
