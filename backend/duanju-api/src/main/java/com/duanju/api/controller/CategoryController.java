package com.duanju.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.duanju.common.core.domain.R;
import com.duanju.drama.domain.Category;
import com.duanju.drama.mapper.CategoryMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "分类接口")
@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryMapper categoryMapper;

    @Value("${duanju.site-id:1}")
    private Integer siteId;

    @Operation(summary = "分类列表")
    @GetMapping("/list")
    public R<List<Category>> list(@RequestParam(required = false) String type) {
        List<Category> list = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getSiteId, siteId)
                .eq(Category::getStatus, "normal")
                .eq(type != null, Category::getType, type)
                .eq(Category::getPid, 0L)
                .orderByDesc(Category::getWeigh));
        return R.ok(list);
    }
}
