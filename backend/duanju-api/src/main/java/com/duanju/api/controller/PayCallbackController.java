package com.duanju.api.controller;

import com.duanju.commerce.service.ResellerService;
import com.duanju.commerce.service.UsableService;
import com.duanju.commerce.service.VipService;
import com.duanju.payment.service.PayService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Hidden
@RestController
@RequestMapping("/api/pay/callback")
@RequiredArgsConstructor
public class PayCallbackController {

    private final PayService payService;
    private final VipService vipService;
    private final UsableService usableService;
    private final ResellerService resellerService;

    @PostMapping("/wechat")
    public Map<String, String> wxCallback(HttpServletRequest request) {
        try {
            StringBuilder body = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) body.append(line);
            }
            Map<String, String> result = payService.handleWxCallback(
                    body.toString(),
                    request.getHeader("Wechatpay-Signature"),
                    request.getHeader("Wechatpay-Timestamp"),
                    request.getHeader("Wechatpay-Nonce"),
                    request.getHeader("Wechatpay-Serial")
            );
            dispatchOrderSuccess(result.get("orderSn"), result.get("transactionId"), body.toString());
            return Map.of("code", "SUCCESS", "message", "成功");
        } catch (Exception e) {
            log.error("微信回调异常", e);
            return Map.of("code", "FAIL", "message", e.getMessage());
        }
    }

    @PostMapping("/alipay")
    public String alipayCallback(HttpServletRequest request) {
        try {
            Map<String, String> params = new HashMap<>();
            request.getParameterMap().forEach((k, v) -> params.put(k, v[0]));
            String orderSn = payService.handleAlipayCallback(params);
            dispatchOrderSuccess(orderSn, params.get("trade_no"), null);
            return "success";
        } catch (Exception e) {
            log.error("支付宝回调异常", e);
            return "fail";
        }
    }

    private void dispatchOrderSuccess(String orderSn, String transactionId, String paymentJson) {
        if (orderSn == null) return;
        if (orderSn.startsWith("VIP")) {
            vipService.handlePaySuccess(orderSn, transactionId, paymentJson);
        } else if (orderSn.startsWith("USE")) {
            usableService.handlePaySuccess(orderSn, transactionId, paymentJson);
        } else if (orderSn.startsWith("RES")) {
            resellerService.handlePaySuccess(orderSn, transactionId, paymentJson);
        } else {
            log.warn("未知订单类型 orderSn={}", orderSn);
        }
    }
}
