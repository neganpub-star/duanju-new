package com.duanju.admin.controller.system;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.duanju.common.core.domain.R;
import com.duanju.common.exception.ServiceException;
import com.duanju.system.domain.SysAdmin;
import com.duanju.system.mapper.SysAdminMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 兼容 RuoYi 前端的个人资料接口（/system/user/profile）
 */
@Tag(name = "【后管】个人资料")
@RestController
@RequestMapping("/system/user/profile")
@RequiredArgsConstructor
public class SystemUserProfileController {

    private final SysAdminMapper adminMapper;

    @Operation(summary = "获取个人资料")
    @GetMapping
    public Map<String, Object> getProfile() {
        SysAdmin admin = currentAdmin();
        Map<String, Object> user = new HashMap<>();
        user.put("userId", admin.getId());
        user.put("userName", admin.getUsername());
        user.put("nickName", admin.getNickname());
        user.put("phonenumber", admin.getMobile());
        user.put("email", admin.getEmail());
        user.put("avatar", admin.getAvatar());
        user.put("sex", "0");
        user.put("createTime", admin.getCreateTime());
        user.put("dept", null);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "操作成功");
        result.put("data", user);
        result.put("roleGroup", "超级管理员");
        result.put("postGroup", "");
        return result;
    }

    @Operation(summary = "修改个人资料")
    @PutMapping
    public R<Void> updateProfile(@RequestBody UpdateProfileReq req) {
        SysAdmin admin = currentAdmin();
        admin.setNickname(req.getNickName());
        admin.setMobile(req.getPhonenumber());
        admin.setEmail(req.getEmail());
        adminMapper.updateById(admin);
        return R.ok();
    }

    @Operation(summary = "修改密码")
    @PutMapping("/updatePwd")
    public R<Void> updatePwd(@RequestBody UpdatePwdReq req) {
        SysAdmin admin = currentAdmin();
        String oldEncPwd = DigestUtil.md5Hex(req.getOldPassword() + admin.getSalt());
        if (!oldEncPwd.equals(admin.getPassword())) {
            throw ServiceException.of("旧密码错误");
        }
        String newSalt = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String newEncPwd = DigestUtil.md5Hex(req.getNewPassword() + newSalt);
        SysAdmin update = new SysAdmin();
        update.setId(admin.getId());
        update.setSalt(newSalt);
        update.setPassword(newEncPwd);
        adminMapper.updateById(update);
        return R.ok();
    }

    @Operation(summary = "上传头像")
    @PostMapping("/avatar")
    public R<Map<String, String>> uploadAvatar(@RequestParam("avatarfile") MultipartFile file) {
        if (file == null || file.isEmpty()) return R.fail("文件不能为空");
        // TODO: 接入真实文件上传服务；当前返回占位 URL
        String imgUrl = "/static/avatar/default.png";
        SysAdmin admin = currentAdmin();
        admin.setAvatar(imgUrl);
        adminMapper.updateById(admin);
        Map<String, String> data = new HashMap<>();
        data.put("imgUrl", imgUrl);
        return R.ok(data);
    }

    private SysAdmin currentAdmin() {
        StpUtil.checkLogin();
        Long adminId = Long.parseLong(StpUtil.getLoginId().toString().replace("admin:", ""));
        SysAdmin admin = adminMapper.selectById(adminId);
        if (admin == null) throw ServiceException.of("管理员不存在");
        return admin;
    }

    @Data
    static class UpdateProfileReq {
        private String nickName;
        private String phonenumber;
        private String email;
        private String sex;
    }

    @Data
    static class UpdatePwdReq {
        private String oldPassword;
        private String newPassword;
    }
}
