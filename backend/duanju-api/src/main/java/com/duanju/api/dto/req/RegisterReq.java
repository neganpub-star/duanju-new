package com.duanju.api.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterReq {

    @NotBlank(message = "error.phone.required")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "error.phone.format")
    private String mobile;

    @NotBlank(message = "error.password.required")
    @Size(min = 6, max = 20, message = "error.password.length")
    private String password;

    private String nickname;
    private Long parentId;
}
