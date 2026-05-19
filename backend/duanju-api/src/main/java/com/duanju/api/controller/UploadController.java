package com.duanju.api.controller;

import com.duanju.common.core.domain.R;
import com.duanju.system.storage.StorageManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.UUID;

/**
 * 用户端文件上传（头像、评论图等）。需登录，鉴权由 SaTokenConfig 配置。
 */
@Slf4j
@Tag(name = "文件上传")
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private final StorageManager storageManager;

    @Operation(summary = "上传文件，返回访问 URL")
    @PostMapping
    public R<String> upload(@RequestParam("file") MultipartFile file) throws Exception {
        String originalName = file.getOriginalFilename();
        String ext = (originalName != null && originalName.contains("."))
                ? originalName.substring(originalName.lastIndexOf("."))
                : "";
        String fileName = "uploads/" + LocalDate.now() + "/" + UUID.randomUUID() + ext;
        String url = storageManager.getStorageService()
                .upload(file.getInputStream(), fileName, file.getContentType());
        log.info("用户端文件上传成功, fileName={}", fileName);
        return R.ok(url);
    }
}
