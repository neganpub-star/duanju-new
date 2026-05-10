package com.duanju.admin.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.duanju.common.core.domain.R;
import com.duanju.common.core.page.PageQuery;
import com.duanju.common.core.page.PageResult;
import com.duanju.system.domain.DramaUser;
import com.duanju.system.mapper.DramaUserMapper;
import com.duanju.system.service.DramaUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Tag(name = "【后管】用户管理")
@RestController
@RequestMapping("/admin/system/user")
@RequiredArgsConstructor
public class AdminUserController {

    private final DramaUserService userService;
    private final DramaUserMapper userMapper;

    @Operation(summary = "用户列表")
    @GetMapping("/list")
    public R<PageResult<DramaUser>> list(PageQuery pageQuery,
                                          @RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) String status,
                                          @RequestParam(defaultValue = "1") Integer siteId) {
        Page<DramaUser> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        LambdaQueryWrapper<DramaUser> wrapper = new LambdaQueryWrapper<DramaUser>()
                .eq(DramaUser::getSiteId, siteId)
                .eq(StringUtils.hasText(status), DramaUser::getStatus, status)
                .and(StringUtils.hasText(keyword), w ->
                        w.like(DramaUser::getMobile, keyword)
                         .or().like(DramaUser::getNickname, keyword))
                .orderByDesc(DramaUser::getId);
        userMapper.selectPage(page, wrapper);
        return R.ok(PageResult.of(page));
    }

    @Operation(summary = "用户详情")
    @GetMapping("/{id}")
    public R<DramaUser> detail(@PathVariable Long id) {
        return R.ok(userService.getById(id));
    }

    @Operation(summary = "禁用/启用用户")
    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        DramaUser user = new DramaUser();
        user.setId(id);
        user.setStatus(status);
        userMapper.updateById(user);
        return R.ok();
    }

    @Operation(summary = "手动充值余额/积分/点数")
    @PostMapping("/{id}/recharge")
    public R<Void> recharge(@PathVariable Long id, @RequestBody RechargeReq req) {
        userService.changeWallet(id, req.getWalletType(), req.getAmount(), "admin_recharge", req.getMemo(), null);
        return R.ok();
    }

    @Data
    static class RechargeReq {
        private String walletType;  // money / score / usable
        private BigDecimal amount;
        private String memo;
    }
}
