package com.duanju.admin.controller.drama;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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

    @Operation(summary = "协议列表（按语言过滤）")
    @GetMapping("/list")
    public R<List<RichText>> list(@RequestParam(defaultValue = "zh-CN") String lang) {
        return R.ok(richTextMapper.selectList(
            new LambdaQueryWrapper<RichText>().eq(RichText::getLang, lang)
                .orderByAsc(RichText::getId)
        ));
    }

    @Operation(summary = "所有语言的文档键列表（去重）")
    @GetMapping("/doc-keys")
    public R<List<RichText>> docKeys() {
        // 返回 zh-CN 的所有文档作为基准列表（包含 doc_key 和 title）
        return R.ok(richTextMapper.selectList(
            new LambdaQueryWrapper<RichText>()
                .eq(RichText::getLang, "zh-CN")
                .orderByAsc(RichText::getId)
        ));
    }

    @Operation(summary = "按 doc_key + lang 获取内容")
    @GetMapping("/doc/{docKey}")
    public R<RichText> getByDocKey(@PathVariable String docKey,
                                    @RequestParam(defaultValue = "zh-CN") String lang) {
        RichText rt = richTextMapper.selectOne(
            new LambdaQueryWrapper<RichText>()
                .eq(RichText::getDocKey, docKey)
                .eq(RichText::getLang, lang)
        );
        if (rt == null) {
            // 返回空对象让前端知道该语言暂无内容
            rt = new RichText();
            rt.setDocKey(docKey);
            rt.setLang(lang);
            rt.setTitle("");
            rt.setContent("");
        }
        return R.ok(rt);
    }

    @Operation(summary = "按 doc_key + lang 保存（不存在则创建）")
    @PutMapping("/doc/{docKey}")
    public R<Void> saveByDocKey(@PathVariable String docKey,
                                 @RequestParam(defaultValue = "zh-CN") String lang,
                                 @RequestBody UpdateReq req) {
        RichText existing = richTextMapper.selectOne(
            new LambdaQueryWrapper<RichText>()
                .eq(RichText::getDocKey, docKey)
                .eq(RichText::getLang, lang)
        );
        if (existing == null) {
            RichText rt = new RichText();
            rt.setDocKey(docKey);
            rt.setLang(lang);
            rt.setTitle(req.getTitle());
            rt.setContent(req.getContent());
            richTextMapper.insert(rt);
        } else {
            existing.setTitle(req.getTitle());
            existing.setContent(req.getContent());
            richTextMapper.updateById(existing);
        }
        return R.ok();
    }

    @Operation(summary = "按 ID 保存（兼容旧接口）")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody UpdateReq req) {
        RichText rt = richTextMapper.selectById(id);
        if (rt == null) return R.fail("记录不存在");
        rt.setTitle(req.getTitle());
        rt.setContent(req.getContent());
        richTextMapper.updateById(rt);
        return R.ok();
    }

    @Data
    public static class UpdateReq {
        private String title;
        private String content;
    }
}
