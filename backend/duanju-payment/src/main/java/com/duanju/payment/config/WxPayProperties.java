package com.duanju.payment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "duanju.wxpay")
public class WxPayProperties {
    private String appId;
    private String mchId;
    private String apiV3Key;
    private String privateKeyPath;
    private String certSerialNo;
    private String notifyUrl;
    /** miniapp appid */
    private String miniAppId;
    /** mp appid */
    private String mpAppId;
}
