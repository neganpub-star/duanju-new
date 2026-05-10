package com.duanju.api.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.duanju.commerce.domain.Usable;
import com.duanju.commerce.domain.UsableOrder;
import com.duanju.commerce.service.UsableService;
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

@Tag(name = "点数套餐接口")
@RestController
@RequestMapping("/api/usable")
@RequiredArgsConstructor
public class UsableController {

    private final UsableService usableService;
    private final PayService payService;

    @Value("${duanju.site-id:1}")
    private Integer siteId;

    @Operation(summary = "点数套餐列表")
    @GetMapping("/list")
    public R<List<Usable>> list() {
        return R.ok(usableService.listAll(siteId));
    }

    @Operation(summary = "购买点数套餐")
    @PostMapping("/buy")
    public R<Map<String, Object>> buy(@RequestBody BuyReq req) {
        long userId = StpUtil.getLoginIdAsLong();
        UsableOrder order = usableService.createOrder(siteId, userId, req.getUsableId(), req.getPayType(), req.getPlatform());
        if ("wallet".equals(req.getPayType())) {
            return R.ok(Map.of("orderSn", order.getOrderSn(), "status", "paid"));
        }
        Map<String, Object> payParams = payService.prepay(order.getOrderSn(), order.getPayFee(),
                "购买点数套餐", req.getPayType(), req.getPlatform(), req.getOpenid());
        payParams.put("orderSn", order.getOrderSn());
        return R.ok(payParams);
    }

    @Data
    static class BuyReq {
        private Long usableId;
        private String payType;
        private String platform;
        private String openid;
    }
}
