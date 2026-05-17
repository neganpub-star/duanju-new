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
import org.springframework.context.i18n.LocaleContextHolder;
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
        data.put("usable_desc", Map.of("content", buildNoticeHtml()));
        return R.ok(data);
    }

    private String buildNoticeHtml() {
        String lang = LocaleContextHolder.getLocale().toLanguageTag();
        if (lang.startsWith("zh-TW") || lang.startsWith("zh-Hant")) {
            return "<p>1. 點數為虛擬商品，購買後不支持退款。</p>" +
                   "<p>2. 點數充值後永久有效，不會過期。</p>" +
                   "<p>3. 每個帳號積分獨立，不可轉讓。</p>" +
                   "<p>4. 未成年人請在家長監護下進行充值消費。</p>";
        } else if (lang.startsWith("en")) {
            return "<p>1. Points are virtual goods and non-refundable once purchased.</p>" +
                   "<p>2. Points never expire after purchase.</p>" +
                   "<p>3. Points are account-specific and non-transferable.</p>" +
                   "<p>4. Minors must recharge under parental supervision.</p>";
        } else {
            return "<p>1. 点数为虚拟商品，购买后不支持退款。</p>" +
                   "<p>2. 点数充值后永久有效，不会过期。</p>" +
                   "<p>3. 每个账号积分独立，不可转让。</p>" +
                   "<p>4. 未成年人请在家长监护下进行充值消费。</p>";
        }
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
