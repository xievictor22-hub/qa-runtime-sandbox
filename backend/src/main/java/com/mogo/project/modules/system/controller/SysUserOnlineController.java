package com.mogo.project.modules.system.controller;

import com.mogo.project.common.response.ApiResponse;
import com.mogo.project.modules.auth.model.LoginUser;
import com.mogo.project.modules.auth.service.TokenService;
import com.mogo.project.modules.system.model.dto.UserOnlineDto;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/monitor/online")
@PreAuthorize("hasAuthority('monitor:online')")
@RequiredArgsConstructor
public class SysUserOnlineController {

    private final RedisTemplate<String, Object> redisTemplate;

    private final TokenService tokenService;

    @Operation(summary = "在线用户列表")
    @PreAuthorize("hasAuthority('monitor:online:list')")
    @GetMapping("/list")
    public ApiResponse<List<UserOnlineDto>> list(String ipaddr, String username) {
        // 1. 获取所有 login_tokens 的 key
        Collection<String> keys = redisTemplate.keys("login_tokens:*");
        List<UserOnlineDto> userOnlineList = new ArrayList<>();
        for (String key : keys) {
            LoginUser user = (LoginUser) redisTemplate.opsForValue().get(key);
            if (user != null) {
                // 过滤查询条件
                if (org.springframework.util.StringUtils.hasText(ipaddr) && !ipaddr.equals(user.getIpaddr())) continue;
                if (org.springframework.util.StringUtils.hasText(username) && !username.equals(user.getUsername())) continue;

                UserOnlineDto dto = new UserOnlineDto();
                dto.setUuid(user.getToken()); // 这是 UUID
                dto.setUsername(user.getUsername());
                dto.setIpaddr(user.getIpaddr());
                dto.setBrowser(user.getBrowser());
                dto.setOs(user.getOs());
                dto.setLoginTime(user.getLoginTime());

                userOnlineList.add(dto);
            }
        }
        // 倒序
        Collections.reverse(userOnlineList);
        return ApiResponse.success(userOnlineList);
    }

    @Operation(summary = "强退用户")
    @PreAuthorize("hasAuthority('monitor:online:forceLogout')")
    @DeleteMapping("/{tokenId}")
    public ApiResponse<Void> forceLogout(@PathVariable String tokenId) {
        // 直接删除 Redis Key，下次 Filter 校验就会失败
        redisTemplate.delete("login_tokens:" + tokenId);
        return ApiResponse.success();
    }
}