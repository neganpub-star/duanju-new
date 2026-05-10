package com.duanju.admin.controller.commerce;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.duanju.commerce.domain.Vip;
import com.duanju.commerce.domain.VipOrder;
import com.duanju.commerce.mapper.VipMapper;
import com.duanju.commerce.mapper.VipOrderMapper;
import com.duanju.common.core.domain.R;
import com.duanju.common.core.page.PageQuery;
import com.duanju.common.core.page.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "【后管】VIP管理")
@RestController
@RequestMapping("/admin/commerce/vip")
@RequiredArgsConstructor
public class AdminVipController {

    private final VipMapper vipMapper;
    private final VipOrderMapper vipOrderMapper;

    @Operation(summary = "VIP套餐列表")
    @GetMapping("/list")
    public R<List<Vip>> list(@RequestParam(defaultValue = "1") Integer siteId) {
        return R.ok(vipMapper.selectList(new LambdaQueryWrapper<Vip>()
                .eq(Vip::getSiteId, siteId).orderByDesc(Vip::getWeigh)));
    }

    @Operation(summary = "新增套餐")
    @PostMapping
    public R<Void> add(@RequestBody Vip vip) {
        vipMapper.insert(vip);
        return R.ok();
    }

    @Operation(summary = "修改套餐")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody Vip vip) {
        vip.setId(id);
        vipMapper.updateById(vip);
        return R.ok();
    }

    @Operation(summary = "删除套餐")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        vipMapper.deleteById(id);
        return R.ok();
    }

    @Operation(summary = "VIP订单列表")
    @GetMapping("/orders")
    public R<PageResult<VipOrder>> orders(PageQuery pageQuery,
                                          @RequestParam(defaultValue = "1") Integer siteId,
                                          @RequestParam(required = false) Integer status) {
        Page<VipOrder> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        vipOrderMapper.selectPage(page, new LambdaQueryWrapper<VipOrder>()
                .eq(VipOrder::getSiteId, siteId)
                .eq(status != null, VipOrder::getStatus, status)
                .orderByDesc(VipOrder::getId));
        return R.ok(PageResult.of(page));
    }
}
