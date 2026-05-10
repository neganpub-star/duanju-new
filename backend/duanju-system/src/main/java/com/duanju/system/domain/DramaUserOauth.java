package com.duanju.system.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.duanju.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("vs_drama_user_oauth")
public class DramaUserOauth extends BaseEntity {

    private Integer siteId;
    private Long userId;

    /** 厂商: wechat / weibo */
    private String provider;

    /** 平台: mp(公众号) / miniapp(小程序) / app */
    private String platform;

    private String unionid;
    private String openid;
    private String nickname;
    private Integer sex;
    private String headimgurl;
    private String sessionKey;
    private String accessToken;
    private String refreshToken;
    private Integer expireIn;
    private Long expireTime;
    private Long loginTime;
    private Integer loginCount;
}
