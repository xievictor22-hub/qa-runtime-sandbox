package com.mogo.project.config.websocket;



import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

@Service
public class WsKickService {

    private final WsSessionRegistry registry;
    private final SimpMessagingTemplate messagingTemplate;

    public WsKickService(WsSessionRegistry registry, SimpMessagingTemplate messagingTemplate) {
        this.registry = registry;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * 同账号同设备重新登录时：踢掉旧 WS session（如果存在）
     */
    public void kickSameDeviceWsIfExists(String username, String deviceId, String reason) {
        if (!StringUtils.hasText(username)) return;
        if (!StringUtils.hasText(deviceId)) deviceId = "unknown";

        String oldSessionId = registry.getSessionId(username, deviceId);
        if (!StringUtils.hasText(oldSessionId)) return;

        // 给“指定 session”发消息：用 sessionId 定向发送
        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.create();
        headers.setSessionId(oldSessionId);
        headers.setLeaveMutable(true);

        messagingTemplate.convertAndSendToUser(
                username,
                "/queue/system",
                Map.of("type", "KICK", "reason", reason),
                headers.getMessageHeaders()
        );
    }
}