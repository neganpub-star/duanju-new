package com.duanju.api.dto.req;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LoginReq {

    /** 手机号，兼容旧前端的 username / phone 字段名 */
    @JsonAlias({"username", "phone"})
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String mobile;

    private String password;

    /** 验证码，兼容旧前端的 code 字段名 */
    @JsonAlias("code")
    private String smsCode;
}
