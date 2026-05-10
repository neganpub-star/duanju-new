package com.duanju.admin.controller.commerce;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.duanju.commerce.domain.Reseller;
import com.duanju.commerce.domain.ResellerBind;
import com.duanju.commerce.domain.ResellerOrder;
import com.duanju.commerce.mapper.ResellerBindMapper;
import com.duanju.commerce.mapper.ResellerMapper;
import com.duanju.commerce.mapper.ResellerOrderMapper;
import com.duanju.common.core.domain.R;
import com.duanju.common.core.page.PageQuery;
import com.duanju.common.core.page.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "【后管】分销管理")
@RestController
@RequestMapping("/admin/commerce/reseller")
@RequiredArgsConstructor
public class AdminResellerController {

    private final ResellerMapper resellerMapper;
    private final ResellerOrderMapper resellerOrderMapper;
    private final ResellerBindMapper resellerBindMapper;

    @Operation(summary = "套餐列表")
    @GetMapping("/list")
    public R<PageResult<Reseller>> list(PageQuery pageQuery,
                                         @RequestParam(defaultValue = "1") Integer siteId) {
        Page<Reseller> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        resellerMapper.selectPage(page, new LambdaQueryWrapper<Reseller>()
                .eq(Reseller::getSiteId, siteId)
                .orderByDesc(Reseller::getWeigh));
        return R.ok(PageResult.of(page));
    }

    @Operation(summary = "新增套餐")
    @PostMapping
    public R<Void> add(@RequestBody Reseller reseller) {
        resellerMapper.insert(reseller);
        return R.ok();
    }

    @Operation(summary = "修改套餐")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody Reseller reseller) {
        reseller.setId(id);
        resellerMapper.updateById(reseller);
        return R.ok();
    }

    @Operation(summary = "删除套餐")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        resellerMapper.deleteById(id);
        return R.ok();
    }

    @Operation(summary = "订单列表")
    @GetMapping("/orders")
    public R<PageResult<ResellerOrder>> orders(PageQuery pageQuery,
                                                @RequestParam(defaultValue = "1") Integer siteId,
                                                @RequestParam(required = false) Long userId,
                                                @RequestParam(required = false) Integer status) {
        Page<ResellerOrder> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        resellerOrderMapper.selectPage(page, new LambdaQueryWrapper<ResellerOrder>()
                .eq(ResellerOrder::getSiteId, siteId)
                .eq(userId != null, ResellerOrder::getUserId, userId)
                .eq(status != null, ResellerOrder::getStatus, status)
                .orderByDesc(ResellerOrder::getId));
        return R.ok(PageResult.of(page));
    }

    @Operation(summary = "绑定关系列表")
    @GetMapping("/binds")
    public R<PageResult<ResellerBind>> binds(PageQuery pageQuery,
                                              @RequestParam(defaultValue = "1") Integer siteId,
                                              @RequestParam(required = false) Long userId) {
        Page<ResellerBind> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        resellerBindMapper.selectPage(page, new LambdaQueryWrapper<ResellerBind>()
                .eq(ResellerBind::getSiteId, siteId)
                .eq(userId != null, ResellerBind::getUserId, userId)
                .orderByDesc(ResellerBind::getId));
        return R.ok(PageResult.of(page));
    }
}
