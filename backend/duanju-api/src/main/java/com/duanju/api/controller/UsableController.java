package com.duanju.api.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.duanju.commerce.domain.Usable;
import com.duanju.commerce.domain.UsableOrder;
import com.duanju.commerce.service.UsableService;
import com.duanju.common.core.domain.R;
import com.duanju.payment.service.PayService;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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
    public R<Map<String, Object>> list() {
        Map<String, Object> data = new HashMap<>();
        data.put("list", usableService.listAll(siteId));
        return R.ok(data);
    }

    @Operation(summary = "购买点数套餐")
    @PostMapping("/buy")
    public R<Map<String, Object>> buy(@RequestBody BuyReq req) {
        long userId = StpUtil.getLoginIdAsLong();
        String payType = req.getPayType() != null ? req.getPayType() : "wechat";
        String platform = req.getPlatform() != null ? req.getPlatform() : "H5";
        UsableOrder order = usableService.createOrder(siteId, userId, req.getUsableId(), payType, platform);

        Map<String, Object> result = new HashMap<>();
        result.put("orderSn", order.getOrderSn());
        result.put("order_sn", order.getOrderSn());
        result.put("platform", platform);

        if ("wallet".equals(payType)) {
            result.put("status", "paid");
            return R.ok(result);
        }
        try {
            Map<String, Object> payParams = payService.prepay(order.getOrderSn(), order.getPayFee(),
                    "购买点数套餐", payType, platform, req.getOpenid());
            result.putAll(payParams);
        } catch (Exception e) {
            result.put("payError", e.getMessage());
        }
        return R.ok(result);
    }

    @Data
    static class BuyReq {
        @JsonProperty("usable_id")
        private Long usableId;
        @JsonProperty("pay_type")
        private String payType;
        @JsonProperty("total_fee")
        private String totalFee;
        private String platform;
        private String openid;
    }
}
