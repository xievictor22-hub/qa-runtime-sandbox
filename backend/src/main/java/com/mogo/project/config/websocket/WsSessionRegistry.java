package com.mogo.project.config.websocket;

import java.util.concurrent.ConcurrentHashMap;

public class WsSessionRegistry {
    private final ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();

    private String key(String username, String deviceId) {
        return username + "::" + deviceId;
    }

    public String getSessionId(String username, String deviceId) {
        return map.get(key(username, deviceId));
    }

    public void put(String username, String deviceId, String sessionId) {
        map.put(key(username, deviceId), sessionId);
    }

    public void remove(String username, String deviceId, String sessionId) {
        String k = key(username, deviceId);
        map.computeIfPresent(k, (kk, old) -> old.equals(sessionId) ? null : old);
    }
}
