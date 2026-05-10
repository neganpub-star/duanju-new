package com.duanju.system.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.duanju.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("vs_admin")
public class SysAdmin extends BaseEntity {

    private String username;
    private String nickname;

    @JsonIgnore
    private String password;

    @JsonIgnore
    private String salt;

    private String avatar;
    private String email;
    private String mobile;
    private Integer loginfailure;
    private Long logintime;
    private String loginip;

    @JsonIgnore
    private String token;

    /** normal / hidden */
    private String status;

    @TableField(exist = false)
    private String newPassword;
}
