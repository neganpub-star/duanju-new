package com.duanju.api.dto.req;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LoginReq {

    /** 手机号，兼容旧前端的 username / phone 字段名 */
    @JsonAlias({"username", "phone"})
    @NotBlank(message = "error.phone.required")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "error.phone.format")
    private String mobile;

    private String password;

    /** 验证码，兼容旧前端的 code 字段名 */
    @JsonAlias("code")
    private String smsCode;
}
