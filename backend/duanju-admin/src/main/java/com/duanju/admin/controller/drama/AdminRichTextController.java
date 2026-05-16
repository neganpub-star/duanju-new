package com.duanju.admin.controller.drama;

import com.duanju.common.core.domain.R;
import com.duanju.drama.domain.RichText;
import com.duanju.drama.mapper.RichTextMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "【后管】协议/富文本管理")
@RestController
@RequestMapping("/admin/richtext")
@RequiredArgsConstructor
public class AdminRichTextController {

    private final RichTextMapper richTextMapper;

    @Operation(summary = "协议列表")
    @GetMapping("/list")
    public R<List<RichText>> list() {
        return R.ok(richTextMapper.selectList(null));
    }

    @Operation(summary = "获取单条")
    @GetMapping("/{id}")
    public R<RichText> getById(@PathVariable Long id) {
        return R.ok(richTextMapper.selectById(id));
    }

    @Operation(summary = "保存协议内容")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody UpdateReq req) {
        RichText existing = richTextMapper.selectById(id);
        RichText rt = existing != null ? existing : new RichText();
        rt.setId(id);
        rt.setTitle(req.getTitle());
        rt.setContent(req.getContent());
        if (existing == null) {
            richTextMapper.insert(rt);
        } else {
            richTextMapper.updateById(rt);
        }
        return R.ok();
    }

    @Data
    public static class UpdateReq {
        private String title;
        private String content;
    }
}
