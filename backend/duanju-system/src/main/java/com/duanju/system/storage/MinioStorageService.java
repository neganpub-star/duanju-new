package com.duanju.system.storage;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;

/**
 * MinIO 存储实现
 */
@Slf4j
@RequiredArgsConstructor
public class MinioStorageService implements StorageService {

    private final MinioClient minioClient;
    private final String bucketName;
    private final String accessUrl;

    @Override
    public String upload(InputStream inputStream, String fileName, String contentType) throws Exception {
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(fileName)
                .stream(inputStream, -1, 10485760)
                .contentType(contentType)
                .build());
        String fileUrl = accessUrl + fileName;
        log.info("MinIO上传成功, fileName={}", fileName);
        return fileUrl;
    }
}
