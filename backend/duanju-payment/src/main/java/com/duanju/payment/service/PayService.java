package com.duanju.payment.service;

import java.math.BigDecimal;
import java.util.Map;

public interface PayService {

    /**
     * 发起预支付
     * @param orderSn  业务订单号
     * @param amount   金额（元）
     * @param subject  商品描述
     * @param payType  wechat / alipay
     * @param platform H5 / wxMiniProgram / wxOfficialAccount / App
     * @param openid   微信openid（微信支付时必填）
     * @return 前端调起支付所需参数
     */
    Map<String, Object> prepay(String orderSn, BigDecimal amount, String subject,
                               String payType, String platform, String openid);

    /**
     * 处理微信支付回调
     * @param body      请求体
     * @param signature 签名头
     * @param timestamp 时间戳头
     * @param nonce     随机串头
     * @param serial    证书序列号头
     * @return 回调处理结果（orderSn -> transactionId）
     */
    Map<String, String> handleWxCallback(String body, String signature,
                                          String timestamp, String nonce, String serial);

    /**
     * 处理支付宝回调
     * @param params 回调参数
     * @return 业务订单号
     */
    String handleAlipayCallback(Map<String, String> params);
}
