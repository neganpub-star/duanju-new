package com.duanju.system.service;

import com.duanju.system.domain.DramaUser;

import java.math.BigDecimal;

public interface DramaUserService {

    DramaUser getById(Long userId);

    DramaUser getByMobile(Integer siteId, String mobile);

    /** 手机号+密码注册 */
    DramaUser register(Integer siteId, String mobile, String password, String nickname, Long parentId);

    /** 密码登录 */
    DramaUser loginByPassword(Integer siteId, String mobile, String password);

    /** 短信验证码登录（验证码由调用方校验） */
    DramaUser loginByMobile(Integer siteId, String mobile);

    /** 微信一键登录，返回用户（不存在则自动注册） */
    DramaUser loginByWechat(Integer siteId, String openid, String unionid, String platform,
                            String nickname, String avatar, Long parentId);

    void updateInfo(Long userId, String nickname, String avatar, Integer gender, String bio);

    /** 变更余额，amount 可正可负，负值时校验余额足够 */
    void changeWallet(Long userId, String walletType, BigDecimal amount, String changeType, String memo, String itemId);

    void updateLastLogin(Long userId, String ip);
}
