package com.icecream.backend.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis令牌黑名单服务（生产环境使用）
 */
@Slf4j
@Service
@Profile("prod")
public class RedisTokenBlacklistService implements TokenBlacklistService {

    private static final String BLACKLIST_KEY_PREFIX = "token:blacklist:";

    private final StringRedisTemplate redisTemplate;

    public RedisTokenBlacklistService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void addToBlacklist(String token, LocalDateTime expiresAt) {
        if (token == null || token.isEmpty()) {
            log.warn("尝试将空令牌加入黑名单");
            return;
        }
        long ttlSeconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), expiresAt);
        if (ttlSeconds <= 0) {
            return;
        }
        String key = BLACKLIST_KEY_PREFIX + token;
        redisTemplate.opsForValue().set(key, "1", ttlSeconds, TimeUnit.SECONDS);
        log.debug("令牌已加入Redis黑名单: tokenHash={}, ttl={}秒", getTokenHash(token), ttlSeconds);
    }

    @Override
    public void addToBlacklist(String token, long expiresInSeconds) {
        if (expiresInSeconds <= 0) {
            return;
        }
        addToBlacklist(token, LocalDateTime.now().plusSeconds(expiresInSeconds));
    }

    @Override
    public boolean isBlacklisted(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        String key = BLACKLIST_KEY_PREFIX + token;
        Boolean exists = redisTemplate.hasKey(key);
        boolean blacklisted = Boolean.TRUE.equals(exists);
        if (blacklisted) {
            log.debug("令牌在Redis黑名单中: tokenHash={}", getTokenHash(token));
        }
        return blacklisted;
    }

    @Override
    public void removeFromBlacklist(String token) {
        if (token == null || token.isEmpty()) {
            return;
        }
        String key = BLACKLIST_KEY_PREFIX + token;
        redisTemplate.delete(key);
        log.debug("令牌已从Redis黑名单移除: tokenHash={}", getTokenHash(token));
    }

    @Override
    @Scheduled(fixedDelay = 3600000L)
    public void cleanupExpiredTokens() {
        // Redis key自带TTL，自动过期，无需主动清理
        log.debug("Redis黑名单无需主动清理，key自带TTL自动过期");
    }

    @Override
    public int getBlacklistSize() {
        Set<String> keys = redisTemplate.keys(BLACKLIST_KEY_PREFIX + "*");
        return keys != null ? keys.size() : 0;
    }

    @Override
    public void clearBlacklist() {
        Set<String> keys = redisTemplate.keys(BLACKLIST_KEY_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("Redis黑名单已清空: 删除了{}个令牌", keys.size());
        }
    }

    @Override
    public void addAllToBlacklist(Iterable<String> tokens, LocalDateTime expiresAt) {
        int count = 0;
        for (String token : tokens) {
            addToBlacklist(token, expiresAt);
            count++;
        }
        log.info("批量添加令牌到Redis黑名单: 添加了{}个令牌", count);
    }

    private String getTokenHash(String token) {
        if (token == null || token.length() < 10) {
            return "invalid";
        }
        return token.substring(0, 10) + "..." + token.substring(token.length() - 10);
    }
}
