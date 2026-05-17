package com.duanju.commerce.vo;

import com.duanju.commerce.domain.UserWalletApply;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class WithdrawVO extends UserWalletApply {

    private String nickname;
    private String mobile;
    private String avatar;
}
