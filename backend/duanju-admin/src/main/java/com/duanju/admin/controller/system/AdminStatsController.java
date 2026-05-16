package com.duanju.admin.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.duanju.commerce.domain.VipOrder;
import com.duanju.commerce.domain.UserWalletApply;
import com.duanju.commerce.mapper.VipOrderMapper;
import com.duanju.commerce.mapper.UserWalletApplyMapper;
import com.duanju.common.core.domain.R;
import com.duanju.drama.domain.Video;
import com.duanju.drama.mapper.VideoMapper;
import com.duanju.system.domain.DramaUser;
import com.duanju.system.mapper.DramaUserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "【后管】数据统计")
@RestController
@RequestMapping("/admin/system/stats")
@RequiredArgsConstructor
public class AdminStatsController {

    private final DramaUserMapper userMapper;
    private final VideoMapper videoMapper;
    private final VipOrderMapper vipOrderMapper;
    private final UserWalletApplyMapper walletApplyMapper;

    @Operation(summary = "首页统计数据")
    @GetMapping
    public R<Map<String, Object>> stats(@RequestParam(defaultValue = "1") Integer siteId) {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        long totalUsers = userMapper.selectCount(new LambdaQueryWrapper<DramaUser>()
                .eq(DramaUser::getSiteId, siteId));
        long todayUsers = userMapper.selectCount(new LambdaQueryWrapper<DramaUser>()
                .eq(DramaUser::getSiteId, siteId)
                .ge(DramaUser::getCreateTime, todayStart));
        long totalVideos = videoMapper.selectCount(new LambdaQueryWrapper<Video>()
                .eq(Video::getSiteId, siteId));
        long normalVideos = videoMapper.selectCount(new LambdaQueryWrapper<Video>()
                .eq(Video::getSiteId, siteId)
                .eq(Video::getStatus, "normal"));
        long pendingWithdraw = walletApplyMapper.selectCount(new LambdaQueryWrapper<UserWalletApply>()
                .eq(UserWalletApply::getSiteId, siteId)
                .eq(UserWalletApply::getStatus, 0));
        long todayOrders = vipOrderMapper.selectCount(new LambdaQueryWrapper<VipOrder>()
                .eq(VipOrder::getSiteId, siteId)
                .eq(VipOrder::getStatus, 1)
                .ge(VipOrder::getCreateTime, todayStart));
        BigDecimal todayRevenue = vipOrderMapper.selectList(new LambdaQueryWrapper<VipOrder>()
                .eq(VipOrder::getSiteId, siteId)
                .eq(VipOrder::getStatus, 1)
                .ge(VipOrder::getCreateTime, todayStart))
                .stream().map(VipOrder::getPayFee)
                .filter(f -> f != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> data = new HashMap<>();
        data.put("totalUsers", totalUsers);
        data.put("todayUsers", todayUsers);
        data.put("totalVideos", totalVideos);
        data.put("normalVideos", normalVideos);
        data.put("pendingWithdraw", pendingWithdraw);
        data.put("todayOrders", todayOrders);
        data.put("todayRevenue", todayRevenue);
        return R.ok(data);
    }
}
