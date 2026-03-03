package com.mogo.project.config.websocket;



import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Map;

@Component
public class WsSessionEvents {

    private final WsSessionRegistry registry;
    private final SimpMessagingTemplate messagingTemplate;

    public WsSessionEvents(WsSessionRegistry registry, SimpMessagingTemplate messagingTemplate) {
        this.registry = registry;
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void onConnect(SessionConnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = sha.getUser();
        if (principal == null) return;

        String username = principal.getName();
        Map<String, Object> attrs = sha.getSessionAttributes();
        String deviceId = attrs == null ? null : (String) attrs.get("deviceId");
        if (!StringUtils.hasText(deviceId)) deviceId = "unknown";

        String newSessionId = sha.getSessionId();
        String oldSessionId = registry.getSessionId(username, deviceId);

        // ✅ 发现同设备旧连接：发 KICK 给旧 session
        if (StringUtils.hasText(oldSessionId) && !oldSessionId.equals(newSessionId)) {
            SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.create();
            headers.setSessionId(oldSessionId);
            headers.setLeaveMutable(true);

            messagingTemplate.convertAndSendToUser(
                    username,
                    "/queue/system",
                    Map.of("type", "KICK", "reason", "same-device-relogin"),
                    headers.getMessageHeaders()
            );
        }

        // ✅ 记录新 session
        registry.put(username, deviceId, newSessionId);
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = sha.getUser();
        if (principal == null) return;

        String username = principal.getName();
        Map<String, Object> attrs = sha.getSessionAttributes();
        String deviceId = attrs == null ? null : (String) attrs.get("deviceId");
        if (!StringUtils.hasText(deviceId)) deviceId = "unknown";

        registry.remove(username, deviceId, sha.getSessionId());
    }
}
