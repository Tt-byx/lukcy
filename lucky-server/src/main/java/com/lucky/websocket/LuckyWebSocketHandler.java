package com.lucky.websocket;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.lucky.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class LuckyWebSocketHandler extends TextWebSocketHandler {

    private final RedisUtil redisUtil;

    // Redis key前缀
    private static final String ONLINE_USERS_KEY = "lucky:online:users";
    private static final String ONLINE_COUNT_KEY = "lucky:online:count";

    // session 映射（本地内存，用于发送消息）
    private final ConcurrentHashMap<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> sessionUserMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Set<String>> userSessionMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessionMap.put(session.getId(), session);

        // 解析 query 参数中的 participantId
        URI uri = session.getUri();
        if (uri != null && uri.getQuery() != null) {
            Map<String, String> params = parseQuery(uri.getQuery());
            String pidStr = params.get("participantId");
            if (pidStr != null) {
                try {
                    Long participantId = Long.parseLong(pidStr);
                    sessionUserMap.put(session.getId(), participantId);
                    userSessionMap.computeIfAbsent(participantId, k -> ConcurrentHashMap.newKeySet())
                            .add(session.getId());

                    // 将用户添加到Redis在线用户集合
                    redisUtil.setAdd(ONLINE_USERS_KEY, participantId.toString());
                    redisUtil.increment(ONLINE_COUNT_KEY);

                    log.info("WebSocket 用户绑定: session={}, participantId={}", session.getId(), participantId);
                } catch (NumberFormatException e) {
                    log.warn("无效的 participantId: {}", pidStr);
                }
            }
        }

        log.info("WebSocket 连接建立: {}", session.getId());
        broadcastOnlineCount();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long participantId = sessionUserMap.remove(session.getId());
        sessionMap.remove(session.getId());
        if (participantId != null) {
            Set<String> sids = userSessionMap.get(participantId);
            if (sids != null) {
                sids.remove(session.getId());
                if (sids.isEmpty()) {
                    userSessionMap.remove(participantId);
                    // 从Redis在线用户集合中移除
                    redisUtil.setRemove(ONLINE_USERS_KEY, participantId.toString());
                    redisUtil.decrement(ONLINE_COUNT_KEY);
                }
            }
        }
        log.info("WebSocket 连接关闭: {}", session.getId());
        broadcastOnlineCount();
    }

    private void broadcastOnlineCount() {
        broadcast("online_update", getOnlineParticipantCount());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        log.info("收到消息: {}", message.getPayload());
    }

    /**
     * 向所有客户端广播消息
     */
    public void broadcast(String type, Object data) {
        JSONObject msg = new JSONObject();
        msg.put("type", type);
        msg.put("data", data);
        String json = msg.toJSONString();

        TextMessage textMessage = new TextMessage(json);
        for (WebSocketSession session : sessionMap.values()) {
            if (session.isOpen()) {
                synchronized (session) {
                    try {
                        session.sendMessage(textMessage);
                    } catch (IOException | IllegalStateException e) {
                        log.error("发送消息失败: {}", session.getId(), e);
                    }
                }
            }
        }
    }

    /**
     * 获取在线用户数量（从Redis获取）
     */
    public int getOnlineParticipantCount() {
        Object count = redisUtil.get(ONLINE_COUNT_KEY);
        if (count != null) {
            return Integer.parseInt(count.toString());
        }
        return 0;
    }

    /**
     * 获取所有在线用户的 participantId 集合（从Redis获取）
     */
    public Set<Long> getOnlineParticipantIds() {
        Set<Object> members = redisUtil.setMembers(ONLINE_USERS_KEY);
        Set<Long> ids = new HashSet<>();
        if (members != null) {
            for (Object member : members) {
                try {
                    ids.add(Long.parseLong(member.toString()));
                } catch (NumberFormatException e) {
                    log.warn("无效的在线用户ID: {}", member);
                }
            }
        }
        return ids;
    }

    /**
     * 向指定用户的所有 session 推送消息
     */
    public void sendToUser(Long participantId, String type, Object data) {
        Set<String> sids = userSessionMap.get(participantId);
        if (sids == null) return;

        JSONObject msg = new JSONObject();
        msg.put("type", type);
        msg.put("data", data);
        String json = msg.toJSONString();
        TextMessage textMessage = new TextMessage(json);

        for (String sid : sids) {
            WebSocketSession session = sessionMap.get(sid);
            if (session != null && session.isOpen()) {
                synchronized (session) {
                    try {
                        session.sendMessage(textMessage);
                    } catch (IOException | IllegalStateException e) {
                        log.error("发送消息失败: {}", sid, e);
                    }
                }
            }
        }
    }

    /**
     * 强制断开指定用户的所有连接
     */
    public void disconnectUser(Long participantId) {
        Set<String> sids = userSessionMap.get(participantId);
        if (sids == null) return;

        for (String sid : new ArrayList<>(sids)) {
            WebSocketSession session = sessionMap.get(sid);
            if (session != null && session.isOpen()) {
                try {
                    session.close(CloseStatus.NORMAL);
                } catch (IOException e) {
                    log.error("关闭连接失败: {}", sid, e);
                }
            }
        }
    }

    public int getOnlineCount() {
        return getOnlineParticipantCount();
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> params = new HashMap<>();
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                params.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                           URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
            }
        }
        return params;
    }
}
