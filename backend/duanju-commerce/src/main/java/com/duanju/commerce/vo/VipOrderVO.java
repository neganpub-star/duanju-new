package com.duanju.commerce.vo;

import com.duanju.commerce.domain.VipOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class VipOrderVO extends VipOrder {

    private String nickname;
    private String mobile;
    private String avatar;
}
