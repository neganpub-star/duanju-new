package com.duanju.system.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duanju.common.exception.ServiceException;
import com.duanju.system.domain.DramaUser;
import com.duanju.system.domain.DramaUserOauth;
import com.duanju.system.mapper.DramaUserMapper;
import com.duanju.system.mapper.DramaUserOauthMapper;
import com.duanju.system.service.DramaUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class DramaUserServiceImpl extends ServiceImpl<DramaUserMapper, DramaUser> implements DramaUserService {

    private final DramaUserOauthMapper oauthMapper;

    @Override
    public DramaUser getById(Long userId) {
        return baseMapper.selectById(userId);
    }

    @Override
    public DramaUser getByMobile(Integer siteId, String mobile) {
        return baseMapper.selectByMobile(siteId, mobile);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DramaUser register(Integer siteId, String mobile, String password, String nickname, Long parentId) {
        DramaUser exist = baseMapper.selectByMobile(siteId, mobile);
        if (exist != null) {
            throw ServiceException.of("手机号已注册");
        }
        String salt = RandomUtil.randomString(6);
        DramaUser user = new DramaUser();
        user.setSiteId(siteId);
        user.setMobile(mobile);
        user.setSalt(salt);
        user.setPassword(encryptPassword(password, salt));
        user.setNickname(nickname != null ? nickname : "用户" + mobile.substring(7));
        user.setMoney(BigDecimal.ZERO);
        user.setScore(BigDecimal.ZERO);
        user.setUsable(BigDecimal.ZERO);
        user.setResellerLevel(0);
        user.setLoginCount(0);
        user.setStatus("normal");
        user.setParentId(parentId);
        baseMapper.insert(user);
        log.info("用户注册成功, mobile: {}, siteId: {}", mobile.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2"), siteId);
        return user;
    }

    @Override
    public DramaUser loginByPassword(Integer siteId, String mobile, String password) {
        DramaUser user = baseMapper.selectByMobile(siteId, mobile);
        if (user == null) {
            throw ServiceException.of("手机号未注册");
        }
        if (!"normal".equals(user.getStatus())) {
            throw ServiceException.of("账号已被禁用");
        }
        if (!encryptPassword(password, user.getSalt()).equals(user.getPassword())) {
            throw ServiceException.of("密码错误");
        }
        return user;
    }

    @Override
    public DramaUser loginByMobile(Integer siteId, String mobile) {
        DramaUser user = baseMapper.selectByMobile(siteId, mobile);
        if (user == null) {
            user = register(siteId, mobile, RandomUtil.randomString(10), null, null);
        }
        if (!"normal".equals(user.getStatus())) {
            throw ServiceException.of("账号已被禁用");
        }
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DramaUser loginByWechat(Integer siteId, String openid, String unionid, String platform,
                                   String nickname, String avatar, Long parentId) {
        DramaUserOauth oauth = oauthMapper.selectByOpenid(siteId, openid, platform);
        DramaUser user;
        if (oauth != null) {
            user = baseMapper.selectById(oauth.getUserId());
            if (user == null || !"normal".equals(user.getStatus())) {
                throw ServiceException.of("账号异常");
            }
            oauth.setLoginTime(Instant.now().getEpochSecond());
            oauth.setLoginCount(oauth.getLoginCount() == null ? 1 : oauth.getLoginCount() + 1);
            oauthMapper.updateById(oauth);
        } else {
            String mobile = "";
            user = new DramaUser();
            user.setSiteId(siteId);
            user.setMobile(mobile);
            user.setNickname(nickname != null ? nickname : "微信用户");
            user.setAvatar(avatar);
            user.setMoney(BigDecimal.ZERO);
            user.setScore(BigDecimal.ZERO);
            user.setUsable(BigDecimal.ZERO);
            user.setResellerLevel(0);
            user.setLoginCount(0);
            user.setStatus("normal");
            user.setParentId(parentId);
            String salt = RandomUtil.randomString(6);
            user.setSalt(salt);
            user.setPassword(encryptPassword(RandomUtil.randomString(16), salt));
            baseMapper.insert(user);

            DramaUserOauth newOauth = new DramaUserOauth();
            newOauth.setSiteId(siteId);
            newOauth.setUserId(user.getId());
            newOauth.setProvider("wechat");
            newOauth.setPlatform(platform);
            newOauth.setOpenid(openid);
            newOauth.setUnionid(unionid);
            newOauth.setNickname(nickname);
            newOauth.setHeadimgurl(avatar);
            newOauth.setLoginTime(Instant.now().getEpochSecond());
            newOauth.setLoginCount(1);
            oauthMapper.insert(newOauth);
            log.info("微信用户注册成功, openid: {}, siteId: {}", openid, siteId);
        }
        return user;
    }

    @Override
    public void updateInfo(Long userId, String nickname, String avatar, Integer gender, String bio) {
        DramaUser user = new DramaUser();
        user.setId(userId);
        user.setNickname(nickname);
        user.setAvatar(avatar);
        user.setGender(gender);
        user.setBio(bio);
        baseMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeWallet(Long userId, String walletType, BigDecimal amount, String changeType, String memo, String itemId) {
        DramaUser user = baseMapper.selectById(userId);
        if (user == null) {
            throw ServiceException.notFound("用户不存在");
        }
        BigDecimal before;
        BigDecimal after;
        switch (walletType) {
            case "money" -> {
                before = user.getMoney();
                after = before.add(amount);
                if (after.compareTo(BigDecimal.ZERO) < 0) {
                    throw ServiceException.of("余额不足");
                }
                DramaUser update = new DramaUser();
                update.setId(userId);
                update.setMoney(after);
                baseMapper.updateById(update);
            }
            case "score" -> {
                before = user.getScore();
                after = before.add(amount);
                if (after.compareTo(BigDecimal.ZERO) < 0) {
                    throw ServiceException.of("积分不足");
                }
                DramaUser update = new DramaUser();
                update.setId(userId);
                update.setScore(after);
                baseMapper.updateById(update);
            }
            case "usable" -> {
                before = user.getUsable();
                after = before.add(amount);
                if (after.compareTo(BigDecimal.ZERO) < 0) {
                    throw ServiceException.of("可用次数不足");
                }
                DramaUser update = new DramaUser();
                update.setId(userId);
                update.setUsable(after);
                baseMapper.updateById(update);
            }
            default -> throw ServiceException.of("不支持的钱包类型: " + walletType);
        }
        log.info("钱包变动 userId={}, type={}, amount={}, changeType={}", userId, walletType, amount, changeType);
    }

    @Override
    public void updateLastLogin(Long userId, String ip) {
        DramaUser update = new DramaUser();
        update.setId(userId);
        update.setLoginTime(Instant.now().getEpochSecond());
        update.setLoginIp(ip);
        baseMapper.updateById(update);
    }

    private String encryptPassword(String password, String salt) {
        return DigestUtil.md5Hex(password + salt);
    }
}
