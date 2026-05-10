package com.duanju.admin.controller.drama;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.duanju.common.core.domain.R;
import com.duanju.common.core.page.PageQuery;
import com.duanju.common.core.page.PageResult;
import com.duanju.drama.domain.Block;
import com.duanju.drama.mapper.BlockMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "【后管】区块管理")
@RestController
@RequestMapping("/admin/drama/block")
@RequiredArgsConstructor
public class AdminBlockController {

    private final BlockMapper blockMapper;

    @Operation(summary = "区块列表")
    @GetMapping("/list")
    public R<PageResult<Block>> list(PageQuery pageQuery, @RequestParam(defaultValue = "1") Integer siteId) {
        Page<Block> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        blockMapper.selectPage(page, new LambdaQueryWrapper<Block>()
                .eq(Block::getSiteId, siteId)
                .orderByDesc(Block::getWeigh));
        return R.ok(PageResult.of(page));
    }

    @Operation(summary = "新增区块")
    @PostMapping
    public R<Void> add(@RequestBody Block block) {
        blockMapper.insert(block);
        return R.ok();
    }

    @Operation(summary = "修改区块")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody Block block) {
        block.setId(id);
        blockMapper.updateById(block);
        return R.ok();
    }

    @Operation(summary = "删除区块")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        blockMapper.deleteById(id);
        return R.ok();
    }
}
