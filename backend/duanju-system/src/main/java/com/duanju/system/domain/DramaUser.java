package com.duanju.system.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.duanju.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("vs_drama_user")
public class DramaUser extends BaseEntity {

    private Integer siteId;
    private String username;

    @JsonIgnore
    private String password;

    @JsonIgnore
    private String salt;

    private String nickname;
    private String avatar;
    private String email;
    private String mobile;
    private Integer gender;
    private String bio;

    /** 余额 */
    private BigDecimal money;

    /** 积分 */
    private BigDecimal score;

    /** 可用次数/点数 */
    private BigDecimal usable;

    /** VIP到期时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime vipExpireTime;

    /** 分销商等级 0=非分销 */
    private Integer resellerLevel;

    /** 分销到期时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime resellerExpireTime;

    /** 上级用户ID（分销关系） */
    private Long parentId;

    private Integer loginCount;
    private Long loginTime;
    private String loginIp;

    /** normal / hidden */
    private String status;

    @TableField(exist = false)
    @JsonIgnore
    private Boolean isVip;

    @TableField(exist = false)
    @JsonIgnore
    private Boolean isReseller;

    public boolean isVipActive() {
        return vipExpireTime != null && vipExpireTime.isAfter(LocalDateTime.now());
    }

    public boolean isResellerActive() {
        return resellerLevel != null && resellerLevel > 0
                && resellerExpireTime != null && resellerExpireTime.isAfter(LocalDateTime.now());
    }

    /** 前端判断VIP状态：1=有效VIP，0=非VIP */
    @JsonGetter("is_vip")
    public Integer getIsVipInt() {
        return isVipActive() ? 1 : 0;
    }

    /** 前端判断分销商状态：1=有效分销商，0=非分销商 */
    @JsonGetter("is_reseller")
    public Integer getIsResellerInt() {
        return isResellerActive() ? 1 : 0;
    }
}
