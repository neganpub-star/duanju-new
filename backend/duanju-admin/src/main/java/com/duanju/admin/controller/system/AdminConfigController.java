package com.duanju.admin.controller.system;

import com.duanju.common.core.domain.R;
import com.duanju.system.domain.SysConfig;
import com.duanju.system.service.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "【后管】系统参数配置")
@RestController
@RequestMapping("/admin/system/config")
@RequiredArgsConstructor
public class AdminConfigController {

    private final SysConfigService configService;

    @Operation(summary = "参数列表（按分组+key排序）")
    @GetMapping("/list")
    public R<List<SysConfig>> list() {
        return R.ok(configService.listAll());
    }

    @Operation(summary = "批量保存参数（Map<key, value>）")
    @PutMapping("/batch")
    public R<Void> batchUpdate(@RequestBody Map<String, String> kvMap) {
        configService.batchUpdate(kvMap);
        return R.ok();
    }

    @Operation(summary = "新增参数配置项")
    @PostMapping
    public R<Void> create(@RequestBody SysConfig config) {
        if (config.getConfigKey() == null || config.getConfigKey().isBlank()) {
            return R.fail("参数键名不能为空");
        }
        configService.create(config);
        return R.ok();
    }

    @Operation(summary = "删除参数配置项")
    @DeleteMapping("/{key}")
    public R<Void> delete(@PathVariable String key) {
        configService.delete(key);
        return R.ok();
    }
}
