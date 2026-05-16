package com.duanju.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.duanju.api.config.I18nUtil;
import com.duanju.common.core.domain.R;
import com.duanju.drama.domain.RichText;
import com.duanju.drama.mapper.RichTextMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@Tag(name = "富文本协议")
@RestController
@RequestMapping("/api/common")
@RequiredArgsConstructor
public class RichTextController {

    private final RichTextMapper richTextMapper;

    @Operation(summary = "获取富文本内容（用户协议/隐私协议等）")
    @GetMapping("/richtext")
    public R<RichText> get(@RequestParam(required = false) Long id,
                           @RequestParam(required = false) String key,
                           @RequestParam(required = false) String lang) {
        // 优先用 key 查询
        if (key != null && !key.isEmpty()) {
            String targetLang = lang != null ? lang : resolveCurrentLang();
            RichText rt = richTextMapper.selectOne(new LambdaQueryWrapper<RichText>()
                    .eq(RichText::getDocKey, key)
                    .eq(RichText::getLang, targetLang));
            if (rt == null && !"zh-CN".equals(targetLang)) {
                // fallback 到简体中文
                rt = richTextMapper.selectOne(new LambdaQueryWrapper<RichText>()
                        .eq(RichText::getDocKey, key)
                        .eq(RichText::getLang, "zh-CN"));
            }
            if (rt == null) return R.ok(emptyRichText(key, I18nUtil.msg("error.richtext.no.content")));
            return R.ok(rt);
        }
        // 兼容旧的 id 查询
        if (id != null) {
            RichText rt = richTextMapper.selectById(id);
            return R.ok(rt != null ? rt : emptyRichText("", I18nUtil.msg("error.richtext.no.content")));
        }
        return R.fail(I18nUtil.msg("error.param.invalid"));
    }

    private String resolveCurrentLang() {
        Locale locale = LocaleContextHolder.getLocale();
        if (locale.equals(Locale.TRADITIONAL_CHINESE)
                || ("zh".equals(locale.getLanguage()) && "TW".equals(locale.getCountry()))
                || ("zh".equals(locale.getLanguage()) && "HK".equals(locale.getCountry()))) {
            return "zh-TW";
        }
        if ("en".equals(locale.getLanguage())) return "en";
        return "zh-CN";
    }

    private RichText emptyRichText(String key, String msg) {
        RichText rt = new RichText();
        rt.setDocKey(key);
        rt.setContent("<p>" + msg + "</p>");
        return rt;
    }
}
