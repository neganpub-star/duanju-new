package com.duanju.admin.controller.system;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.duanju.common.core.domain.R;
import com.duanju.common.exception.ServiceException;
import com.duanju.system.domain.SysAdmin;
import com.duanju.system.mapper.SysAdminMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Tag(name = "【后管】登录")
@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final SysAdminMapper adminMapper;

    @Operation(summary = "管理员登录")
    @PostMapping("/login")
    public R<Map<String, Object>> login(@Valid @RequestBody LoginReq req) {
        SysAdmin admin = adminMapper.selectByUsername(req.getUsername());
        if (admin == null) {
            throw ServiceException.of("账号不存在");
        }
        if (!"normal".equals(admin.getStatus())) {
            throw ServiceException.of("账号已被禁用");
        }
        String encPwd = DigestUtil.md5Hex(req.getPassword() + admin.getSalt());
        if (!encPwd.equals(admin.getPassword())) {
            throw ServiceException.of("密码错误");
        }
        StpUtil.login("admin:" + admin.getId());
        admin.setLogintime(Instant.now().getEpochSecond());
        adminMapper.updateById(admin);

        Map<String, Object> result = new HashMap<>();
        result.put("token", StpUtil.getTokenValue());
        result.put("adminId", admin.getId());
        result.put("username", admin.getUsername());
        result.put("nickname", admin.getNickname());
        result.put("avatar", admin.getAvatar());
        log.info("管理员登录, username={}", admin.getUsername());
        return R.ok(result);
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public R<Void> logout() {
        StpUtil.logout();
        return R.ok();
    }

    @Operation(summary = "获取当前管理员信息")
    @GetMapping("/info")
    public R<SysAdmin> info() {
        StpUtil.checkLogin();
        String loginId = StpUtil.getLoginId().toString();
        Long adminId = Long.parseLong(loginId.replace("admin:", ""));
        SysAdmin admin = adminMapper.selectById(adminId);
        return R.ok(admin);
    }

    @Data
    static class LoginReq {
        @NotBlank(message = "用户名不能为空")
        private String username;

        @NotBlank(message = "密码不能为空")
        private String password;
    }
}
