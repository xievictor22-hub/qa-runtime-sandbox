package com.mogo.project.modules.auth.service;

import com.mogo.project.common.util.IpUtils;
import com.mogo.project.config.websocket.WsKickService;
import com.mogo.project.config.websocket.WsSessionRegistry;
import com.mogo.project.modules.auth.dto.LoginDto;
import com.mogo.project.modules.auth.model.LoginUser;
import com.mogo.project.modules.system.mapper.SysMenuMapper;
import com.mogo.project.modules.system.service.SysLoginInforService;
import eu.bitwalker.useragentutils.UserAgent;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    @Resource
    private AuthenticationManager authenticationManager;


    @Resource
    private SysLoginInforService loginInforService;

    @Resource
    private TokenService tokenService;

    private final WsKickService wsKickService;


    /**
     * 登录接口
     * @param loginDto 登录参数
     * @return 包含 Token 的 Map
     */
    public Map<String, Object> login(LoginDto loginDto) {
        // 获取 IP
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String ip = IpUtils.getIpAddr(request);
        String deviceId = request.getHeader("X-Device-Id");
        try {

        // 1. 调用 Security 的认证方法 (会调用 UserDetailsServiceImpl)
        // 如果密码错误，这里会直接抛出 BadCredentialsException，由全局异常处理捕获
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword())
        );

        // 2. 认证通过，将用户信息放入上下文 (虽然 Controller 结束后就销毁了，但这是一个好习惯)
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. 获取用户信息
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();

        // 4. 生成 Token  由TokenService 接管
//        String token = jwtUtils.generateToken(loginUser.getUsername());

        // 补充信息
        UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
        loginUser.setIpaddr(ip);
        loginUser.setBrowser(userAgent.getBrowser().getName());
        loginUser.setOs(userAgent.getOperatingSystem().getName());
        // ★★★ 生成 Token (存入 Redis) ★★★
        Map<String, Object> tokenMap = tokenService.createToken(loginUser,deviceId);

        // 5. 构造返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("token", tokenMap.get("token"));
        result.put("tokenHead", "Bearer "); // 前端通常需要拼接这个前缀
        // 也可以返回用户基本信息，如头像、昵称等,内部含有roles
        result.put("userInfo", loginUser.getSysUser());
        result.put("permissions", loginUser.getPermissions());
        result.put("refreshToken", tokenMap.get("refresh_token"));
        // ★★★ 2. 记录登录成功日志 ★★★
        loginInforService.recordLogininfor(loginDto.getUsername(), "1", "登录成功", ip);

        // ✅ 3) 踢掉同设备旧 WS（如果旧 WS 还在线）
        wsKickService.kickSameDeviceWsIfExists(loginUser.getUsername(), deviceId, "same-device-login");

        return result;
        }catch (Exception e){
            // ★★★ 3. 记录登录失败日志 ★★★
            // 注意：这里记录后需要继续抛出异常，否则前端收不到报错
            loginInforService.recordLogininfor(loginDto.getUsername(), "0", e.getMessage(), ip);
            throw e;
        }
    }

}