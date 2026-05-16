package com.duanju.api.controller;

import com.duanju.common.core.domain.R;
import com.duanju.drama.domain.RichText;
import com.duanju.drama.mapper.RichTextMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "富文本协议")
@RestController
@RequestMapping("/api/common")
@RequiredArgsConstructor
public class RichTextController {

    private final RichTextMapper richTextMapper;

    @Operation(summary = "获取富文本内容（用户协议/隐私协议等）")
    @GetMapping("/richtext")
    public R<RichText> get(@RequestParam Long id) {
        RichText rt = richTextMapper.selectById(id);
        if (rt == null) {
            RichText empty = new RichText();
            empty.setId(id);
            empty.setTitle("内容");
            empty.setContent("<p>暂无内容，请联系管理员配置。</p>");
            return R.ok(empty);
        }
        return R.ok(rt);
    }
}
