package com.duanju.admin.controller.system;

import com.duanju.common.core.domain.R;
import com.duanju.system.storage.StorageManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Tag(name = "【后管】文件上传")
@RestController
@RequestMapping("/admin/upload")
@RequiredArgsConstructor
public class AdminUploadController {

    private final StorageManager storageManager;

    @Operation(summary = "上传文件，返回访问URL")
    @PostMapping
    public R<String> upload(@RequestParam("file") MultipartFile file) throws Exception {
        String originalName = file.getOriginalFilename();
        String ext = (originalName != null && originalName.contains("."))
                ? originalName.substring(originalName.lastIndexOf("."))
                : "";
        String fileName = "uploads/" + LocalDate.now() + "/" + UUID.randomUUID() + ext;
        String url = storageManager.getStorageService()
                .upload(file.getInputStream(), fileName, file.getContentType());
        log.info("后管文件上传成功, fileName={}", fileName);
        return R.ok(url);
    }
}
