package com.duanju.commerce.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.duanju.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("vs_drama_reseller_order")
public class ResellerOrder extends BaseEntity {
    private Integer siteId;
    private Long resellerId;
    private String orderSn;
    private Long userId;
    private Integer times;
    private Integer status;
    private BigDecimal totalFee;
    private BigDecimal payFee;
    private String transactionId;
    private String paymentJson;
    private String payType;
    private String platform;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTime;
    private String remark;
    private String ext;
}
