package com.duanju.api.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.duanju.commerce.domain.Reseller;
import com.duanju.commerce.domain.ResellerOrder;
import com.duanju.commerce.service.ResellerService;
import com.duanju.common.core.domain.R;
import com.duanju.payment.service.PayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "分销接口")
@RestController
@RequestMapping("/api/reseller")
@RequiredArgsConstructor
public class ResellerController {

    private final ResellerService resellerService;
    private final PayService payService;

    @Value("${duanju.site-id:1}")
    private Integer siteId;

    @Operation(summary = "分销套餐列表")
    @GetMapping("/list")
    public R<List<Reseller>> list() {
        return R.ok(resellerService.listAll(siteId));
    }

    @Operation(summary = "购买分销资格")
    @PostMapping("/buy")
    public R<Map<String, Object>> buy(@RequestBody BuyReq req) {
        long userId = StpUtil.getLoginIdAsLong();
        ResellerOrder order = resellerService.createOrder(siteId, userId, req.getResellerId(), req.getPayType(), req.getPlatform());
        if ("wallet".equals(req.getPayType())) {
            return R.ok(Map.of("orderSn", order.getOrderSn(), "status", "paid"));
        }
        Map<String, Object> payParams = payService.prepay(order.getOrderSn(), order.getPayFee(),
                "购买分销资格", req.getPayType(), req.getPlatform(), req.getOpenid());
        payParams.put("orderSn", order.getOrderSn());
        return R.ok(payParams);
    }

    @Operation(summary = "绑定邀请人（仅首次）")
    @PostMapping("/bind")
    public R<Void> bind(@RequestParam Long inviterUserId) {
        long userId = StpUtil.getLoginIdAsLong();
        resellerService.bindInviter(userId, inviterUserId);
        return R.ok();
    }

    @Data
    static class BuyReq {
        private Long resellerId;
        private String payType;
        private String platform;
        private String openid;
    }
}
