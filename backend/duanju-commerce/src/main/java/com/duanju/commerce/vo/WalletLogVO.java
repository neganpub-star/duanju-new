package com.duanju.commerce.vo;

import com.duanju.commerce.domain.UserWalletLog;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class WalletLogVO extends UserWalletLog {

    private String nickname;
    private String mobile;
    private String avatar;
}
