package com.icecream.backend.security;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

/**
 * WebSocket 握手处理器
 * 从请求属性中提取已认证的用户信息
 */
public class ChatHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {
        // 如果JWT过滤器已经认证了用户，SecurityContext中有认证信息
        // Spring会自动使用SecurityContext中的Principal
        return request.getPrincipal();
    }
}
