package com.duanju.api.controller;

import com.duanju.commerce.domain.Vip;
import com.duanju.commerce.domain.VipOrder;
import com.duanju.commerce.service.VipService;
import com.duanju.common.core.domain.R;
import com.duanju.common.utils.SecurityUtil;
import com.duanju.payment.service.PayService;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "VIP接口")
@RestController
@RequestMapping("/api/vip")
@RequiredArgsConstructor
public class VipController {

    private final VipService vipService;
    private final PayService payService;

    @Value("${duanju.site-id:1}")
    private Integer siteId;

    @Operation(summary = "VIP套餐列表")
    @GetMapping("/list")
    public R<Map<String, Object>> list() {
        Map<String, Object> data = new HashMap<>();
        data.put("list", vipService.listAll(siteId));
        return R.ok(data);
    }

    @Operation(summary = "购买VIP（发起支付）")
    @PostMapping("/buy")
    public R<Map<String, Object>> buy(@Valid @RequestBody BuyVipReq req) {
        Long userId = SecurityUtil.getUserId();
        VipOrder order = vipService.createOrder(siteId, userId, req.getVipId(), req.getPayType(), req.getPlatform());

        Map<String, Object> result = new HashMap<>();
        result.put("orderSn", order.getOrderSn());
        result.put("order_sn", order.getOrderSn());
        result.put("platform", req.getPlatform());
        result.put("status", order.getStatus());

        if ("wallet".equals(req.getPayType())) {
            result.put("payResult", "success");
            return R.ok(result);
        }

        try {
            Map<String, Object> payParams = payService.prepay(
                    order.getOrderSn(), order.getPayFee(), "购买VIP-" + order.getDays() + "天",
                    req.getPayType(), req.getPlatform(), req.getOpenid());
            result.putAll(payParams);
        } catch (Exception e) {
            result.put("payError", e.getMessage());
        }
        return R.ok(result);
    }

    @Data
    static class BuyVipReq {
        @NotNull(message = "套餐ID不能为空")
        @JsonProperty("vip_id")
        private Long vipId;
        private String payType = "wechat";
        @JsonProperty("total_fee")
        private String totalFee;
        private String platform = "H5";
        private String openid;
    }
}
