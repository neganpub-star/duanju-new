package com.duanju.api.controller;

import com.duanju.common.core.domain.R;
import com.duanju.common.utils.SecurityUtil;
import com.duanju.system.domain.DramaUser;
import com.duanju.system.service.DramaUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户接口")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final DramaUserService userService;

    @Operation(summary = "获取个人信息")
    @GetMapping("/info")
    public R<DramaUser> info() {
        return R.ok(userService.getById(SecurityUtil.getUserId()));
    }

    @Operation(summary = "修改个人信息")
    @PutMapping("/info")
    public R<Void> updateInfo(@RequestBody UpdateInfoReq req) {
        userService.updateInfo(SecurityUtil.getUserId(), req.getNickname(), req.getAvatar(), req.getGender(), req.getBio());
        return R.ok();
    }

    @Data
    static class UpdateInfoReq {
        private String nickname;
        private String avatar;
        private Integer gender;
        private String bio;
    }
}
