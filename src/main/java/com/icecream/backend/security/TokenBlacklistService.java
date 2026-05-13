package com.icecream.backend.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 令牌黑名单服务
 * 管理已失效的JWT令牌（登出、刷新等场景）
 * 注意：当前使用内存实现，适合开发环境。生产环境应使用Redis等分布式缓存
 */
@Slf4j
@Service
public class TokenBlacklistService {

    // 使用ConcurrentHashMap存储黑名单令牌及其过期时间
    private final Map<String, LocalDateTime> blacklist = new ConcurrentHashMap<>();

    // 清理任务执行间隔（毫秒）
    private static final long CLEANUP_INTERVAL = 3600000L; // 1小时

    /**
     * 将令牌加入黑名单
     *
     * @param token JWT令牌
     * @param expiresAt 令牌过期时间
     */
    public void addToBlacklist(String token, LocalDateTime expiresAt) {
        if (token == null || token.isEmpty()) {
            log.warn("尝试将空令牌加入黑名单");
            return;
        }

        blacklist.put(token, expiresAt);
        log.debug("令牌已加入黑名单: tokenHash={}, expiresAt={}", getTokenHash(token), expiresAt);
    }

    /**
     * 将令牌加入黑名单（使用当前时间计算过期时间）
     *
     * @param token JWT令牌
     * @param expiresInSeconds 过期时间（秒）
     */
    public void addToBlacklist(String token, long expiresInSeconds) {
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expiresInSeconds);
        addToBlacklist(token, expiresAt);
    }

    /**
     * 检查令牌是否在黑名单中
     *
     * @param token JWT令牌
     * @return 是否在黑名单中
     */
    public boolean isBlacklisted(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        LocalDateTime expiresAt = blacklist.get(token);
        if (expiresAt == null) {
            return false;
        }

        // 检查令牌是否已过期
        if (LocalDateTime.now().isAfter(expiresAt)) {
            // 移除已过期的令牌
            blacklist.remove(token);
            log.debug("已移除过期黑名单令牌: tokenHash={}", getTokenHash(token));
            return false;
        }

        log.debug("令牌在黑名单中: tokenHash={}", getTokenHash(token));
        return true;
    }

    /**
     * 从黑名单中移除令牌
     *
     * @param token JWT令牌
     */
    public void removeFromBlacklist(String token) {
        if (token == null || token.isEmpty()) {
            return;
        }

        blacklist.remove(token);
        log.debug("令牌已从黑名单移除: tokenHash={}", getTokenHash(token));
    }

    /**
     * 清理过期的黑名单令牌
     * 定时任务，每小时执行一次
     */
    @Scheduled(fixedDelay = CLEANUP_INTERVAL)
    public void cleanupExpiredTokens() {
        log.debug("开始清理过期黑名单令牌");

        int initialSize = blacklist.size();
        LocalDateTime now = LocalDateTime.now();

        // 遍历黑名单，移除已过期的令牌
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

    /**
     * 获取黑名单大小
     *
     * @return 黑名单中的令牌数量
     */
    public int getBlacklistSize() {
        return blacklist.size();
    }

    /**
     * 清空黑名单（主要用于测试）
     */
    public void clearBlacklist() {
        blacklist.clear();
        log.info("黑名单已清空");
    }

    /**
     * 获取令牌的哈希值（用于日志记录，不存储完整令牌）
     *
     * @param token JWT令牌
     * @return 令牌哈希值
     */
    private String getTokenHash(String token) {
        if (token == null || token.length() < 10) {
            return "invalid";
        }
        // 取令牌的前10个和后10个字符作为哈希标识
        return token.substring(0, 10) + "..." + token.substring(token.length() - 10);
    }

    /**
     * 批量将令牌加入黑名单（用于批量登出等场景）
     *
     * @param tokens 令牌数组
     * @param expiresAt 过期时间
     */
    public void addAllToBlacklist(Iterable<String> tokens, LocalDateTime expiresAt) {
        for (String token : tokens) {
            addToBlacklist(token, expiresAt);
        }
        log.info("批量添加令牌到黑名单: 添加了{}个令牌", countIterable(tokens));
    }

    /**
     * 计算可迭代对象的元素数量
     *
     * @param iterable 可迭代对象
     * @return 元素数量
     */
    private int countIterable(Iterable<String> iterable) {
        int count = 0;
        for (String ignored : iterable) {
            count++;
        }
        return count;
    }
}