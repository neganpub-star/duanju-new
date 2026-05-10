package com.duanju.api.dto.resp;

import lombok.Data;

@Data
public class LoginResp {
    private String token;
    private Long userId;
    private String nickname;
    private String avatar;
    private String mobile;
    private Boolean isVip;
    private Boolean isReseller;
}
