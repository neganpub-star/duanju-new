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
@TableName("vs_drama_vip_order")
public class VipOrder extends BaseEntity {

    private Integer siteId;
    private Long vipId;
    private String orderSn;
    private Long userId;

    /** 购买天数 */
    private Integer days;

    /** -2=交易关闭 -1=已取消 0=未支付 1=已支付 2=已完成 */
    private Integer status;

    private BigDecimal totalFee;
    private BigDecimal payFee;
    private String transactionId;
    private String paymentJson;

    /** wechat / alipay / wallet / score / cryptocard / system */
    private String payType;

    /** H5 / Web / wxOfficialAccount / wxMiniProgram / App */
    private String platform;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTime;

    private String remark;
    private String ext;
}
