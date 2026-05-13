package com.icecream.backend.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存令牌黑名单服务（开发/测试环境使用）
 */
@Slf4j
@Service
@Profile("!prod")
public class InMemoryTokenBlacklistService implements TokenBlacklistService {

    private final Map<String, LocalDateTime> blacklist = new ConcurrentHashMap<>();

    private static final long CLEANUP_INTERVAL = 3600000L;

    @Override
    public void addToBlacklist(String token, LocalDateTime expiresAt) {
        if (token == null || token.isEmpty()) {
            log.warn("尝试将空令牌加入黑名单");
            return;
        }
        blacklist.put(token, expiresAt);
        log.debug("令牌已加入黑名单: tokenHash={}, expiresAt={}", getTokenHash(token), expiresAt);
    }

    @Override
    public void addToBlacklist(String token, long expiresInSeconds) {
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expiresInSeconds);
        addToBlacklist(token, expiresAt);
    }

    @Override
    public boolean isBlacklisted(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        LocalDateTime expiresAt = blacklist.get(token);
        if (expiresAt == null) {
            return false;
        }
        if (LocalDateTime.now().isAfter(expiresAt)) {
            blacklist.remove(token);
            log.debug("已移除过期黑名单令牌: tokenHash={}", getTokenHash(token));
            return false;
        }
        log.debug("令牌在黑名单中: tokenHash={}", getTokenHash(token));
        return true;
    }

    @Override
    public void removeFromBlacklist(String token) {
        if (token == null || token.isEmpty()) {
            return;
        }
        blacklist.remove(token);
        log.debug("令牌已从黑名单移除: tokenHash={}", getTokenHash(token));
    }

    @Override
    @Scheduled(fixedDelay = CLEANUP_INTERVAL)
    public void cleanupExpiredTokens() {
        log.debug("开始清理过期黑名单令牌");
        int initialSize = blacklist.size();
        LocalDateTime now = LocalDateTime.now();
        blacklist.entrySet().removeIf(entry -> {
            boolean expired = now.isAfter(entry.getValue());
            if (expired) {
                log.debug("清理过期黑名单令牌: tokenHash={}", getTokenHash(entry.getKey()));
            }
            return expired;
        });
        int removedCount = initialSize - blacklist.size();
        log.info("黑名单清理完成: 清理了{}个过期令牌，剩余{}个令牌", removedCount, blacklist.size());
    }

    @Override
    public int getBlacklistSize() {
        return blacklist.size();
    }

    @Override
    public void clearBlacklist() {
        blacklist.clear();
        log.info("黑名单已清空");
    }

    @Override
    public void addAllToBlacklist(Iterable<String> tokens, LocalDateTime expiresAt) {
        int count = 0;
        for (String token : tokens) {
            addToBlacklist(token, expiresAt);
            count++;
        }
        log.info("批量添加令牌到黑名单: 添加了{}个令牌", count);
    }

    private String getTokenHash(String token) {
        if (token == null || token.length() < 10) {
            return "invalid";
        }
        return token.substring(0, 10) + "..." + token.substring(token.length() - 10);
    }
}
