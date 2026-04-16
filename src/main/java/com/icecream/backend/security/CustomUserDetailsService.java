package com.icecream.backend.security;

import com.icecream.backend.mapper.UserMapper;
import com.icecream.backend.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

/**
 * 自定义用户详情服务
 * 实现Spring Security的UserDetailsService接口，从数据库加载用户信息
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;

    /**
     * 根据用户名加载用户详情
     *
     * @param username 用户名
     * @return 用户详情对象
     * @throws UsernameNotFoundException 用户未找到异常
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("加载用户详情: username={}", username);

        // 根据用户名查找用户
        Optional<User> userOpt = userMapper.findByUsername(username);
        if (!userOpt.isPresent()) {
            log.warn("用户未找到: username={}", username);
            throw new UsernameNotFoundException("用户未找到: " + username);
        }

        User user = userOpt.get();

        // 检查用户状态
        if (user.getStatus() == 0) {
            log.warn("用户已被禁用: username={}", username);
            throw new UsernameNotFoundException("用户已被禁用: " + username);
        }

        log.debug("用户详情加载成功: username={}, role={}", username, user.getRole());

        // 创建Spring Security UserDetails对象
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(user.getRole())))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(user.getStatus() == 0)
                .build();
    }

    /**
     * 根据用户ID加载用户详情
     *
     * @param userId 用户ID
     * @return 用户详情对象
     * @throws UsernameNotFoundException 用户未找到异常
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        log.debug("根据ID加载用户详情: userId={}", userId);

        Optional<User> userOpt = userMapper.findById(userId);
        if (!userOpt.isPresent()) {
            log.warn("用户未找到: userId={}", userId);
            throw new UsernameNotFoundException("用户未找到: " + userId);
        }

        User user = userOpt.get();

        // 检查用户状态
        if (user.getStatus() == 0) {
            log.warn("用户已被禁用: userId={}", userId);
            throw new UsernameNotFoundException("用户已被禁用: " + userId);
        }

        log.debug("用户详情加载成功: userId={}, username={}, role={}", userId, user.getUsername(), user.getRole());

        // 创建Spring Security UserDetails对象
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(user.getRole())))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(user.getStatus() == 0)
                .build();
    }

    /**
     * 根据用户名或邮箱加载用户详情（用于登录）
     *
     * @param usernameOrEmail 用户名或邮箱
     * @return 用户详情对象
     * @throws UsernameNotFoundException 用户未找到异常
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsernameOrEmail(String usernameOrEmail) throws UsernameNotFoundException {
        log.debug("根据用户名或邮箱加载用户详情: usernameOrEmail={}", usernameOrEmail);

        Optional<User> userOpt = userMapper.findByUsernameOrEmail(usernameOrEmail);
        if (!userOpt.isPresent()) {
            log.warn("用户未找到: usernameOrEmail={}", usernameOrEmail);
            throw new UsernameNotFoundException("用户未找到: " + usernameOrEmail);
        }

        User user = userOpt.get();

        // 检查用户状态
        if (user.getStatus() == 0) {
            log.warn("用户已被禁用: usernameOrEmail={}", usernameOrEmail);
            throw new UsernameNotFoundException("用户已被禁用: " + usernameOrEmail);
        }

        log.debug("用户详情加载成功: usernameOrEmail={}, username={}, role={}",
                usernameOrEmail, user.getUsername(), user.getRole());

        // 创建Spring Security UserDetails对象
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(user.getRole())))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(user.getStatus() == 0)
                .build();
    }
}