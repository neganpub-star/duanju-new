package com.duanju.admin.controller.commerce;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.duanju.commerce.domain.Usable;
import com.duanju.commerce.domain.UsableOrder;
import com.duanju.commerce.mapper.UsableMapper;
import com.duanju.commerce.mapper.UsableOrderMapper;
import com.duanju.common.core.domain.R;
import com.duanju.common.core.page.PageQuery;
import com.duanju.common.core.page.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "【后管】点数套餐管理")
@RestController
@RequestMapping("/admin/commerce/usable")
@RequiredArgsConstructor
public class AdminUsableController {

    private final UsableMapper usableMapper;
    private final UsableOrderMapper usableOrderMapper;

    @Operation(summary = "套餐列表")
    @GetMapping("/list")
    public R<PageResult<Usable>> list(PageQuery pageQuery,
                                       @RequestParam(defaultValue = "1") Integer siteId) {
        Page<Usable> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        usableMapper.selectPage(page, new LambdaQueryWrapper<Usable>()
                .eq(Usable::getSiteId, siteId)
                .orderByDesc(Usable::getWeigh));
        return R.ok(PageResult.of(page));
    }

    @Operation(summary = "新增套餐")
    @PostMapping
    public R<Void> add(@RequestBody Usable usable) {
        usableMapper.insert(usable);
        return R.ok();
    }

    @Operation(summary = "修改套餐")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody Usable usable) {
        usable.setId(id);
        usableMapper.updateById(usable);
        return R.ok();
    }

    @Operation(summary = "删除套餐")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        usableMapper.deleteById(id);
        return R.ok();
    }

    @Operation(summary = "订单列表")
    @GetMapping("/orders")
    public R<PageResult<UsableOrder>> orders(PageQuery pageQuery,
                                              @RequestParam(defaultValue = "1") Integer siteId,
                                              @RequestParam(required = false) Long userId,
                                              @RequestParam(required = false) Integer status) {
        Page<UsableOrder> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        usableOrderMapper.selectPage(page, new LambdaQueryWrapper<UsableOrder>()
                .eq(UsableOrder::getSiteId, siteId)
                .eq(userId != null, UsableOrder::getUserId, userId)
                .eq(status != null, UsableOrder::getStatus, status)
                .orderByDesc(UsableOrder::getId));
        return R.ok(PageResult.of(page));
    }
}
