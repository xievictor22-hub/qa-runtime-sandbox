package com.mogo.project.modules.system.controller;

import com.mogo.project.common.annotation.Anonymous;
import com.mogo.project.common.response.ApiResponse;
import com.mogo.project.modules.auth.model.LoginUser;
import com.mogo.project.modules.auth.service.TokenService;
import com.mogo.project.modules.system.model.entity.SysUser;
import com.mogo.project.modules.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "06. 个人中心")
@RestController
@RequestMapping("/system/user/profile")
@Anonymous
@RequiredArgsConstructor
public class SysProfileController {

    private final SysUserService sysUserService;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @Anonymous
    @Operation(summary = "获取个人信息")
    @GetMapping
    public ApiResponse<SysUser> profile() {
        LoginUser loginUser = getLoginUser();
        SysUser user = sysUserService.getById(loginUser.getSysUser().getId());
        user.setPassword(null); // 脱敏
        return ApiResponse.success(user);
    }

    @Anonymous
    @Operation(summary = "修改基本信息")
    @PutMapping
    public ApiResponse<Void> updateProfile(@RequestBody SysUser user) {
        LoginUser loginUser = getLoginUser();
        SysUser currentUser = loginUser.getSysUser();

        currentUser.setNickname(user.getNickname());
        currentUser.setPhone(user.getPhone());
        currentUser.setEmail(user.getEmail());
        currentUser.setGender(user.getGender());
        if (sysUserService.updateById(currentUser)) {
            // 2. ★★★ 核心修复：更新 Redis 缓存 ★★★
            // loginUser 持有的是 currentUser 的引用，所以上面的 set 操作已经修改了内存中的值
            // 我们只需要调用 refreshToken 将最新的对象序列化写入 Redis 覆盖旧数据即可
            tokenService.refreshToken(loginUser);
        }
        return ApiResponse.success();
    }
    @Anonymous
    @Operation(summary = "修改头像")
    @PutMapping("/avatar")
    public ApiResponse<String> updateAvatar(@RequestParam("avatar") String avatar) {
        LoginUser loginUser = getLoginUser();
        SysUser currentUser = loginUser.getSysUser();
        currentUser.setAvatar(avatar);
        if (sysUserService.updateById(currentUser)) {
            // 刷新 Redis
            tokenService.refreshToken(loginUser);
        }
        return ApiResponse.success(avatar);
    }

    @Anonymous
    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public ApiResponse<Void> updatePassword(String oldPassword, String newPassword) {
        LoginUser loginUser = getLoginUser();
        String password = sysUserService.getById(loginUser.getSysUser().getId()).getPassword();

        if (!passwordEncoder.matches(oldPassword, password)) {
            return ApiResponse.error("旧密码错误");
        }
        if (passwordEncoder.matches(newPassword, password)) {
            return ApiResponse.error("新密码不能与旧密码相同");
        }
        SysUser user = new SysUser();
        user.setId(loginUser.getSysUser().getId());
        user.setPassword(passwordEncoder.encode(newPassword));

        if(sysUserService.updateById(user)){
            // 更新缓存中的密码
            loginUser.getSysUser().setPassword(user.getPassword());
            tokenService.refreshToken(loginUser);
        }
        return ApiResponse.success();
    }

    // 辅助方法
    private LoginUser getLoginUser() {
        return (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}