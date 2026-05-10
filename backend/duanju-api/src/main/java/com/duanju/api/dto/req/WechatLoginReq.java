package com.duanju.api.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WechatLoginReq {

    @NotBlank(message = "openid 不能为空")
    private String openid;

    private String unionid;

    /** mp=公众号 miniapp=小程序 app=App */
    @NotBlank(message = "平台不能为空")
    private String platform;

    private String nickname;
    private String avatar;
    private Long parentId;
}
