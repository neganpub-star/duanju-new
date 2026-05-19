package com.duanju.admin.controller.system;

import com.duanju.common.core.domain.R;
import com.duanju.common.exception.ServiceException;
import com.duanju.system.storage.StorageManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Tag(name = "【后管】文件上传")
@RestController
@RequestMapping("/admin/upload")
@RequiredArgsConstructor
public class AdminUploadController {

    private final StorageManager storageManager;

    /** 视频默认扩展名 */
    private static final String DEFAULT_VIDEO_EXT = ".mp4";

    @Operation(summary = "上传文件，返回访问 URL")
    @PostMapping
    public R<String> upload(@RequestParam("file") MultipartFile file) throws Exception {
        String ext = extractExt(file.getOriginalFilename());
        String fileName = buildObjectKey(ext);
        String url = storageManager.getStorageService()
                .upload(file.getInputStream(), fileName, file.getContentType());
        log.info("后管文件上传成功, fileName={}", fileName);
        return R.ok(url);
    }

    /**
     * 下载外部 URL 到本平台 OSS，返回新的 OSS URL。
     * 由前端按钮主动触发，因此这里不再做"是否本平台 OSS"的判断。
     */
    @Operation(summary = "下载外部 URL 到本平台 OSS")
    @PostMapping("/remote")
    public R<String> remote(@RequestBody RemoteReq req) throws Exception {
        String raw = req == null ? null : req.getUrl();
        if (raw == null || raw.isBlank()) {
            throw new ServiceException("URL 不能为空");
        }
        raw = raw.trim();
        if (!raw.startsWith("http://") && !raw.startsWith("https://")) {
            throw new ServiceException("URL 必须以 http(s):// 开头");
        }

        URI uri;
        try {
            uri = safeToUri(raw);
        } catch (Exception e) {
            throw new ServiceException("URL 格式不合法: " + e.getMessage());
        }

        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(300000); // 大文件下载留 5 分钟
            conn.setInstanceFollowRedirects(true);
            conn.setRequestProperty("User-Agent", "duanju-fetch/1.0");
            int code = conn.getResponseCode();
            if (code < 200 || code >= 300) {
                throw new ServiceException("拉取远程文件失败, HTTP " + code);
            }
            String contentType = conn.getContentType();
            String ext = extractExt(raw);
            if (ext.isEmpty()) ext = guessExtFromContentType(contentType);
            String fileName = buildObjectKey(ext);
            try (InputStream in = conn.getInputStream()) {
                String ossUrl = storageManager.getStorageService().upload(in, fileName, contentType);
                log.info("远程下载入库成功, src={}, dst={}", raw, ossUrl);
                return R.ok(ossUrl);
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("远程下载入库失败, url={}", raw, e);
            throw new ServiceException("远程文件入库失败: " + e.getMessage());
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    /**
     * 对原始 URL 做安全转 URI：
     * 把 path 按 "/" 拆段后对每段做 percent-encoding，兼容中文、空格、$、单引号等
     */
    private URI safeToUri(String raw) {
        int schemeEnd = raw.indexOf("://");
        if (schemeEnd < 0) return URI.create(raw);
        int pathStart = raw.indexOf('/', schemeEnd + 3);
        String prefix = pathStart < 0 ? raw : raw.substring(0, pathStart);
        String pathAndQuery = pathStart < 0 ? "" : raw.substring(pathStart);
        int qIdx = pathAndQuery.indexOf('?');
        String path = qIdx < 0 ? pathAndQuery : pathAndQuery.substring(0, qIdx);
        String query = qIdx < 0 ? null : pathAndQuery.substring(qIdx + 1);
        StringBuilder enc = new StringBuilder();
        if (!path.isEmpty()) {
            String[] segs = path.split("/", -1);
            for (int i = 0; i < segs.length; i++) {
                if (i > 0) enc.append('/');
                // URLEncoder 用的是 application/x-www-form-urlencoded 规则：空格会变 '+'，路径里需要还原为 %20
                enc.append(URLEncoder.encode(segs[i], StandardCharsets.UTF_8).replace("+", "%20"));
            }
        }
        return URI.create(prefix + enc + (query != null ? "?" + query : ""));
    }

    /** 从文件名或 URL 路径里截取扩展名（含点），如 ".mp4"；取不到返回空串 */
    private String extractExt(String nameOrUrl) {
        if (nameOrUrl == null || nameOrUrl.isBlank()) return "";
        String path = nameOrUrl;
        int q = path.indexOf('?');
        if (q >= 0) path = path.substring(0, q);
        int slash = path.lastIndexOf('/');
        if (slash >= 0) path = path.substring(slash + 1);
        try { path = URLDecoder.decode(path, StandardCharsets.UTF_8); } catch (Exception ignored) {}
        int dot = path.lastIndexOf('.');
        if (dot < 0) return "";
        String ext = path.substring(dot);
        return ext.length() > 8 ? "" : ext;
    }

    /** 根据 contentType 猜扩展名 */
    private String guessExtFromContentType(String contentType) {
        if (contentType == null) return DEFAULT_VIDEO_EXT;
        String ct = contentType.toLowerCase();
        if (ct.contains("mp4")) return ".mp4";
        if (ct.contains("x-m4v")) return ".m4v";
        if (ct.contains("quicktime")) return ".mov";
        if (ct.contains("webm")) return ".webm";
        if (ct.contains("matroska")) return ".mkv";
        if (ct.contains("mpegurl")) return ".m3u8";
        return DEFAULT_VIDEO_EXT;
    }

    /** 构造对象存储 key：uploads/yyyy-MM-dd/uuid.ext */
    private String buildObjectKey(String ext) {
        return "uploads/" + LocalDate.now() + "/" + UUID.randomUUID() + (ext == null ? "" : ext);
    }

    @lombok.Data
    public static class RemoteReq {
        private String url;
    }
}
