package com.icecream.backend.util;

import com.icecream.backend.mapper.UserMapper;
import com.icecream.backend.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Optional;

/**
 * 安全工具类
 * 提供从Spring Security SecurityContext获取当前用户信息的静态方法
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityUtil {

    private static UserMapper staticUserMapper;
    private final UserMapper userMapper;

    @PostConstruct
    private void init() {
        SecurityUtil.staticUserMapper = this.userMapper;
    }

    /**
     * 获取当前认证信息
     *
     * @return 当前认证信息，如果未认证则返回null
     */
    public static Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 获取当前用户详情
     *
     * @return 当前用户详情，如果未认证则返回null
     */
    public static UserDetails getCurrentUserDetails() {
        Authentication authentication = getCurrentAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return (UserDetails) principal;
        }

        return null;
    }

    /**
     * 获取当前用户名
     *
     * @return 当前用户名，如果未认证则返回null
     * @throws IllegalStateException 如果用户未认证
     */
    public static String getCurrentUsername() {
        UserDetails userDetails = getCurrentUserDetails();
        if (userDetails == null) {
            throw new IllegalStateException("用户未认证");
        }
        return userDetails.getUsername();
    }

    /**
     * 获取当前用户ID
     * 根据用户名从数据库查询用户ID
     *
     * @return 当前用户ID
     * @throws IllegalStateException 如果用户未认证或用户不存在
     */
    public static Long getCurrentUserId() {
        String username = getCurrentUsername();

        // 从数据库查询用户ID
        Optional<User> userOpt = staticUserMapper.findByUsername(username);
        if (!userOpt.isPresent()) {
            log.error("用户不存在: username={}", username);
            throw new IllegalStateException("用户不存在: " + username);
        }

        Long userId = userOpt.get().getId();
        log.debug("获取当前用户ID: username={}, userId={}", username, userId);
        return userId;
    }

    /**
     * 获取当前用户ID，如果未认证则返回null
     *
     * @return 当前用户ID，如果未认证则返回null
     */
    public static Long getCurrentUserIdOrNull() {
        try {
            return getCurrentUserId();
        } catch (IllegalStateException e) {
            return null;
        }
    }

    /**
     * 获取当前用户实体
     *
     * @return 当前用户实体
     * @throws IllegalStateException 如果用户未认证或用户不存在
     */
    public static User getCurrentUser() {
        String username = getCurrentUsername();

        Optional<User> userOpt = staticUserMapper.findByUsername(username);
        if (!userOpt.isPresent()) {
            log.error("用户不存在: username={}", username);
            throw new IllegalStateException("用户不存在: " + username);
        }

        return userOpt.get();
    }

    /**
     * 检查当前用户是否具有指定角色
     *
     * @param role 角色名称，如 "ROLE_USER", "ROLE_ADMIN"
     * @return 是否具有指定角色
     */
    public static boolean hasRole(String role) {
        UserDetails userDetails = getCurrentUserDetails();
        if (userDetails == null) {
            return false;
        }

        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals(role));
    }

    /**
     * 检查当前用户是否是管理员
     *
     * @return 是否是管理员
     */
    public static boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }

    /**
     * 检查当前用户是否是普通用户
     *
     * @return 是否是普通用户
     */
    public static boolean isUser() {
        return hasRole("ROLE_USER");
    }

    /**
     * 检查给定用户ID是否是当前用户
     *
     * @param userId 要检查的用户ID
     * @return 是否是当前用户
     */
    public static boolean isCurrentUser(Long userId) {
        try {
            Long currentUserId = getCurrentUserId();
            return currentUserId.equals(userId);
        } catch (IllegalStateException e) {
            return false;
        }
    }

    /**
     * 检查当前用户是否已认证
     *
     * @return 是否已认证
     */
    public static boolean isAuthenticated() {
        Authentication authentication = getCurrentAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

    /**
     * 检查当前用户是否未认证
     *
     * @return 是否未认证
     */
    public static boolean isAnonymous() {
        return !isAuthenticated();
    }

    /**
     * 获取当前用户的角色
     *
     * @return 当前用户的角色，如果未认证则返回null
     */
    public static String getCurrentUserRole() {
        UserDetails userDetails = getCurrentUserDetails();
        if (userDetails == null) {
            return null;
        }

        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null);
    }

    /**
     * 验证当前用户是否具有访问权限
     * 检查用户是否是管理员或者是资源的所有者
     *
     * @param resourceOwnerId 资源所有者的用户ID
     * @return 是否具有访问权限
     */
    public static boolean hasAccess(Long resourceOwnerId) {
        if (isAdmin()) {
            return true;
        }

        return isCurrentUser(resourceOwnerId);
    }

    /**
     * 验证当前用户是否具有访问权限，如果不具备则抛出异常
     *
     * @param resourceOwnerId 资源所有者的用户ID
     * @throws IllegalStateException 如果不具备访问权限
     */
    public static void validateAccess(Long resourceOwnerId) {
        if (!hasAccess(resourceOwnerId)) {
            throw new IllegalStateException("没有访问权限");
        }
    }
}