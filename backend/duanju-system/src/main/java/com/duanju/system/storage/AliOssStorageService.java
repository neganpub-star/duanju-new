package com.duanju.system.storage;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;

/**
 * 阿里云 OSS 存储实现
 */
@Slf4j
@RequiredArgsConstructor
public class AliOssStorageService implements StorageService {

    private final String endpoint;
    private final String accessKeyId;
    private final String accessKeySecret;
    private final String bucketName;
    private final String accessUrl;

    @Override
    public String upload(InputStream inputStream, String fileName, String contentType) throws Exception {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            ossClient.putObject(bucketName, fileName, inputStream, metadata);
            String fileUrl = accessUrl + fileName;
            log.info("OSS上传成功, fileName={}", fileName);
            return fileUrl;
        } finally {
            ossClient.shutdown();
        }
    }
}
