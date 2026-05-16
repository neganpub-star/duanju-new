package com.duanju.payment.service.impl;

import com.duanju.common.exception.ServiceException;
import com.duanju.payment.config.AlipayProperties;
import com.duanju.payment.config.WxPayProperties;
import com.duanju.payment.service.PayService;
import com.github.binarywang.wxpay.bean.notify.WxPayNotifyV3Result;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderV3Request;
import com.github.binarywang.wxpay.bean.result.WxPayUnifiedOrderV3Result;
import com.github.binarywang.wxpay.bean.result.enums.TradeTypeEnum;
import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayServiceImpl implements PayService {

    private final WxPayProperties wxPayProperties;
    private final AlipayProperties alipayProperties;

    @Autowired(required = false)
    private MessageSource messageSource;

    private String msg(String key) {
        if (messageSource == null) return key;
        try {
            return messageSource.getMessage(key, null, key, LocaleContextHolder.getLocale());
        } catch (Exception e) {
            return key;
        }
    }

    @Override
    public Map<String, Object> prepay(String orderSn, BigDecimal amount, String subject,
                                       String payType, String platform, String openid) {
        return switch (payType) {
            case "wechat" -> wxPrepay(orderSn, amount, subject, platform, openid);
            case "alipay" -> alipayPrepay(orderSn, amount, subject);
            default -> throw ServiceException.of(msg("error.pay.type") + ": " + payType);
        };
    }

    private Map<String, Object> wxPrepay(String orderSn, BigDecimal amount,
                                          String subject, String platform, String openid) {
        try {
            WxPayService wxPayService = buildWxPayService();
            int totalFee = amount.multiply(new BigDecimal(100)).intValue();

            WxPayUnifiedOrderV3Request request = new WxPayUnifiedOrderV3Request();
            request.setOutTradeNo(orderSn);
            request.setDescription(subject);
            request.setNotifyUrl(wxPayProperties.getNotifyUrl());
            WxPayUnifiedOrderV3Request.Amount amountObj = new WxPayUnifiedOrderV3Request.Amount();
            amountObj.setTotal(totalFee);
            request.setAmount(amountObj);

            Map<String, Object> result = new HashMap<>();
            switch (platform) {
                case "wxMiniProgram", "wxOfficialAccount" -> {
                    WxPayUnifiedOrderV3Request.Payer payer = new WxPayUnifiedOrderV3Request.Payer();
                    payer.setOpenid(openid);
                    request.setPayer(payer);
                    WxPayUnifiedOrderV3Result.JsapiResult jsapi =
                            wxPayService.createOrderV3(TradeTypeEnum.JSAPI, request);
                    result.put("timeStamp", jsapi.getTimeStamp());
                    result.put("nonceStr", jsapi.getNonceStr());
                    result.put("package", jsapi.getPackageValue());
                    result.put("signType", jsapi.getSignType());
                    result.put("paySign", jsapi.getPaySign());
                }
                case "H5" -> {
                    WxPayUnifiedOrderV3Result h5 =
                            wxPayService.createOrderV3(TradeTypeEnum.H5, request);
                    result.put("h5Url", h5.getH5Url());
                }
                case "App" -> {
                    WxPayUnifiedOrderV3Result.AppResult app =
                            wxPayService.createOrderV3(TradeTypeEnum.APP, request);
                    result.put("prepayId", app.getPrepayId());
                    result.put("appId", app.getAppid());
                    result.put("partnerId", app.getPartnerId());
                    result.put("nonceStr", app.getNoncestr());
                    result.put("timeStamp", app.getTimestamp());
                    result.put("sign", app.getSign());
                }
                default -> throw ServiceException.of(msg("error.pay.platform") + ": " + platform);
            }
            return result;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("微信预支付失败 orderSn={}", orderSn, e);
            throw ServiceException.of(msg("error.pay.failed") + ": " + e.getMessage());
        }
    }

    private Map<String, Object> alipayPrepay(String orderSn, BigDecimal amount, String subject) {
        Map<String, Object> result = new HashMap<>();
        result.put("orderSn", orderSn);
        result.put("payType", "alipay");
        return result;
    }

    @Override
    public Map<String, String> handleWxCallback(String body, String signature,
                                                  String timestamp, String nonce, String serial) {
        try {
            WxPayService wxPayService = buildWxPayService();
            WxPayNotifyV3Result.DecryptNotifyResult notifyData =
                    wxPayService.parseOrderNotifyV3Result(body, null).getResult();
            Map<String, String> result = new HashMap<>();
            result.put("orderSn", notifyData.getOutTradeNo());
            result.put("transactionId", notifyData.getTransactionId());
            result.put("paymentJson", body);
            return result;
        } catch (Exception e) {
            log.error("微信支付回调处理失败", e);
            throw ServiceException.of("回调处理失败: " + e.getMessage());
        }
    }

    @Override
    public String handleAlipayCallback(Map<String, String> params) {
        return params.get("out_trade_no");
    }

    private WxPayService buildWxPayService() {
        WxPayConfig config = new WxPayConfig();
        config.setAppId(wxPayProperties.getAppId());
        config.setMchId(wxPayProperties.getMchId());
        config.setApiV3Key(wxPayProperties.getApiV3Key());
        config.setPrivateKeyPath(wxPayProperties.getPrivateKeyPath());
        config.setCertSerialNo(wxPayProperties.getCertSerialNo());
        WxPayServiceImpl service = new WxPayServiceImpl();
        service.setConfig(config);
        return service;
    }
}
