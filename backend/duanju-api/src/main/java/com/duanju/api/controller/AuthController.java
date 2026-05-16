package com.duanju.api.controller;

import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import com.duanju.api.config.I18nUtil;
import com.duanju.api.dto.req.LoginReq;
import com.duanju.api.dto.req.RegisterReq;
import com.duanju.api.dto.req.WechatLoginReq;
import com.duanju.api.dto.resp.LoginResp;
import com.duanju.common.core.domain.R;
import com.duanju.common.exception.ServiceException;
import com.duanju.system.domain.DramaUser;
import com.duanju.system.service.DramaUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "认证接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final DramaUserService userService;

    @Value("${duanju.site-id:1}")
    private Integer siteId;

    @Operation(summary = "手机号+密码登录")
    @PostMapping("/login")
    public R<LoginResp> login(@Valid @RequestBody LoginReq req, HttpServletRequest request) {
        DramaUser user = userService.loginByPassword(siteId, req.getMobile(), req.getPassword());
        return R.ok(buildLoginResp(user, request));
    }

    @Operation(summary = "手机验证码登录/注册")
    @PostMapping("/sms-login")
    public R<LoginResp> smsLogin(@Valid @RequestBody LoginReq req, HttpServletRequest request) {
        // 验证码由 SmsController 校验后存 Redis，这里只需验证是否已通过
        DramaUser user = userService.loginByMobile(siteId, req.getMobile());
        return R.ok(buildLoginResp(user, request));
    }

    @Operation(summary = "微信授权登录")
    @PostMapping("/wechat-login")
    public R<LoginResp> wechatLogin(@Valid @RequestBody WechatLoginReq req, HttpServletRequest request) {
        DramaUser user = userService.loginByWechat(
                siteId, req.getOpenid(), req.getUnionid(), req.getPlatform(),
                req.getNickname(), req.getAvatar(), req.getParentId());
        return R.ok(buildLoginResp(user, request));
    }

    @Operation(summary = "注册")
    @PostMapping("/register")
    public R<LoginResp> register(@Valid @RequestBody RegisterReq req, HttpServletRequest request) {
        DramaUser user = userService.register(siteId, req.getMobile(), req.getPassword(), req.getNickname(), req.getParentId());
        return R.ok(buildLoginResp(user, request));
    }

    @Operation(summary = "发送验证码（开发环境固定返回成功）")
    @PostMapping("/send-sms")
    public R<Void> sendSms(@RequestBody LoginReq req) {
        // TODO: 接入真实短信服务后在此处发送验证码
        if (req.getMobile() == null || req.getMobile().isBlank()) {
            return R.fail(I18nUtil.msg("error.phone.required"));
        }
        log.info("发送验证码: mobile={}", req.getMobile());
        return R.ok();
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public R<Void> logout() {
        StpUtil.logout();
        return R.ok();
    }

    @Operation(summary = "刷新 Token")
    @PostMapping("/refresh")
    public R<String> refresh() {
        StpUtil.checkLogin();
        String newToken = StpUtil.getTokenValue();
        return R.ok(newToken);
    }

    private LoginResp buildLoginResp(DramaUser user, HttpServletRequest request) {
        StpUtil.login(user.getId(), SaLoginModel.create().setExtra("siteId", siteId));
        userService.updateLastLogin(user.getId(), request.getRemoteAddr());
        LoginResp resp = new LoginResp();
        resp.setToken(StpUtil.getTokenValue());
        resp.setUserId(user.getId());
        resp.setNickname(user.getNickname());
        resp.setAvatar(user.getAvatar());
        resp.setMobile(user.getMobile());
        resp.setIsVip(user.isVipActive());
        resp.setIsReseller(user.isResellerActive());
        return resp;
    }
}
