package com.duanju.system.storage;

import java.io.InputStream;

/**
 * 存储服务接口（支持阿里云OSS / MinIO）
 */
public interface StorageService {

    /**
     * 上传文件
     *
     * @param inputStream 文件流
     * @param fileName    存储路径/文件名，如 uploads/2024-01-01/xxxx.jpg
     * @param contentType MIME类型
     * @return 文件访问 URL
     */
    String upload(InputStream inputStream, String fileName, String contentType) throws Exception;
}
