package com.duanju.api.controller;

import com.duanju.common.core.domain.R;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 兼容旧前端的废弃接口桩，统一返回成功（data=null）
 */
@RestController
@RequestMapping("/api/noop")
public class NoopController {

    @RequestMapping
    public R<Void> noop() {
        return R.ok();
    }
}
