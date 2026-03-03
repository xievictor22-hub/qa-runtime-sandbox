package com.mogo.project.config.websocket;

// package com.mogo.project.websocket;


import com.mogo.project.modules.auth.model.LoginUser;
import com.mogo.project.modules.auth.service.TokenService;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;


public class WsAuthChannelInterceptor implements ChannelInterceptor {

    private final TokenService tokenService;

    public WsAuthChannelInterceptor(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) return message;

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String auth = accessor.getFirstNativeHeader("Authorization");
            String deviceId = accessor.getFirstNativeHeader("X-Device-Id");

            if (!StringUtils.hasText(auth) || !auth.startsWith("Bearer ")) {
                // 你也可以选择直接抛异常拒绝连接
                return message;
            }

            String token = auth.substring(7);

            // 这里复用你现有 TokenService 的解析逻辑：拿到 LoginUser
            LoginUser loginUser = tokenService.getLoginUser(token);
            if (loginUser == null) {
                return message; // 或抛异常拒绝
            }

            // ✅ 关键：绑定 Principal（后续 /user/** 才能按用户路由）
            Authentication user =
                    new UsernamePasswordAuthenticationToken(loginUser.getUsername(), null, null);
            accessor.setUser(user);

            // ✅ 把 deviceId 记到 session attributes（后面 connect/disconnect 事件要用）
            accessor.getSessionAttributes().put("deviceId", deviceId);
        }

        return message;
    }
}
