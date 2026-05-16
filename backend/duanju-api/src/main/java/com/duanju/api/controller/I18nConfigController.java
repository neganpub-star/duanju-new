package com.duanju.api.controller;

import com.duanju.common.core.domain.R;
import com.duanju.system.service.SysConfigService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Tag(name = "国际化配置接口")
@RestController
@RequestMapping("/api/common/i18n")
@RequiredArgsConstructor
public class I18nConfigController {

    private final SysConfigService sysConfigService;
    private final ObjectMapper objectMapper;

    @Operation(summary = "获取 i18n 配置（支持的语种列表、是否显示切换器）")
    @GetMapping("/config")
    public R<Map<String, Object>> config() {
        String rawLangs    = sysConfigService.getValue("i18n.supported_langs", "zh-CN,zh-TW,en");
        String showSwitcher = sysConfigService.getValue("i18n.show_switcher", "1");
        String defaultLang  = sysConfigService.getValue("i18n.default_lang", "zh-CN");

        List<String> langs = parseLangs(rawLangs);

        Map<String, Object> data = new HashMap<>();
        data.put("supported_langs", langs);
        data.put("show_switcher", !"0".equals(showSwitcher));
        data.put("default_lang", defaultLang);
        return R.ok(data);
    }

    /** 支持 JSON 数组格式 ["zh-CN","en"] 或逗号分隔 zh-CN,en 两种写法 */
    private List<String> parseLangs(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of("zh-CN", "zh-TW", "en");
        }
        String trimmed = raw.trim();
        if (trimmed.startsWith("[")) {
            try {
                return objectMapper.readValue(trimmed, new TypeReference<List<String>>() {});
            } catch (Exception e) {
                log.warn("Failed to parse i18n.supported_langs as JSON: {}", trimmed);
            }
        }
        return Arrays.stream(trimmed.split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
    }
}
