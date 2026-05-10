package com.duanju.admin.controller.drama;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.duanju.common.core.domain.R;
import com.duanju.drama.domain.Category;
import com.duanju.drama.mapper.CategoryMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "【后管】分类管理")
@RestController
@RequestMapping("/admin/drama/category")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryMapper categoryMapper;

    @Operation(summary = "分类列表（树形）")
    @GetMapping("/list")
    public R<List<Category>> list(@RequestParam(defaultValue = "1") Integer siteId) {
        return R.ok(categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getSiteId, siteId)
                .orderByDesc(Category::getWeigh)));
    }

    @Operation(summary = "新增分类")
    @PostMapping
    public R<Void> add(@RequestBody Category category) {
        categoryMapper.insert(category);
        return R.ok();
    }

    @Operation(summary = "修改分类")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody Category category) {
        category.setId(id);
        categoryMapper.updateById(category);
        return R.ok();
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        categoryMapper.deleteById(id);
        return R.ok();
    }
}
