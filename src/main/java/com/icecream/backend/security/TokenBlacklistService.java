package com.icecream.backend.security;

import java.time.LocalDateTime;

/**
 * 令牌黑名单服务接口
 */
public interface TokenBlacklistService {

    void addToBlacklist(String token, LocalDateTime expiresAt);
    void addToBlacklist(String token, long expiresInSeconds);
    boolean isBlacklisted(String token);
    void removeFromBlacklist(String token);
    void cleanupExpiredTokens();
    int getBlacklistSize();
    void clearBlacklist();
    void addAllToBlacklist(Iterable<String> tokens, LocalDateTime expiresAt);
}
