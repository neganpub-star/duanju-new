package com.duanju.api.controller;

import com.duanju.commerce.domain.Vip;
import com.duanju.commerce.domain.VipOrder;
import com.duanju.commerce.service.VipService;
import com.duanju.common.core.domain.R;
import com.duanju.common.utils.SecurityUtil;
import com.duanju.payment.service.PayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
    public R<List<Vip>> list() {
        return R.ok(vipService.listAll(siteId));
    }

    @Operation(summary = "购买VIP（发起支付）")
    @PostMapping("/buy")
    public R<Map<String, Object>> buy(@Valid @RequestBody BuyVipReq req) {
        Long userId = SecurityUtil.getUserId();
        VipOrder order = vipService.createOrder(siteId, userId, req.getVipId(), req.getPayType(), req.getPlatform());

        Map<String, Object> result = new HashMap<>();
        result.put("orderSn", order.getOrderSn());
        result.put("status", order.getStatus());

        // 余额支付已处理，直接返回成功
        if ("wallet".equals(req.getPayType())) {
            result.put("payResult", "success");
            return R.ok(result);
        }

        // 第三方支付需返回支付参数
        Map<String, Object> payParams = payService.prepay(
                order.getOrderSn(), order.getPayFee(), "购买VIP-" + order.getDays() + "天",
                req.getPayType(), req.getPlatform(), req.getOpenid());
        result.putAll(payParams);
        return R.ok(result);
    }

    @Data
    static class BuyVipReq {
        @NotNull(message = "套餐ID不能为空")
        private Long vipId;
        @NotBlank(message = "支付方式不能为空")
        private String payType;
        @NotBlank(message = "平台不能为空")
        private String platform;
        private String openid;
    }
}
