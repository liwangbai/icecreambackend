package com.icecream.backend.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT认证过滤器
 * 拦截所有请求，从Authorization头提取JWT令牌，验证令牌并设置认证信息到SecurityContext
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    /**
     * 过滤器核心方法，处理每个请求
     *
     * @param request HTTP请求
     * @param response HTTP响应
     * @param filterChain 过滤器链
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 从请求中提取JWT令牌
            String jwt = getJwtFromRequest(request);

            // 如果令牌存在且有效
            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
                // 从令牌中提取用户名
                String username = jwtTokenProvider.getUsernameFromToken(jwt);

                // 检查令牌类型是否为访问令牌
                String tokenType = jwtTokenProvider.getTokenType(jwt);
                if (!"access".equals(tokenType)) {
                    log.warn("令牌类型错误，期望access令牌，实际为: {}", tokenType);
                    filterChain.doFilter(request, response);
                    return;
                }

                // 加载用户详情
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

                // 创建认证令牌
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                // 设置认证详情
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 设置认证信息到SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("JWT认证成功: username={}, uri={}", username, request.getRequestURI());
            } else {
                log.debug("JWT令牌无效或不存在: uri={}", request.getRequestURI());
            }
        } catch (Exception ex) {
            log.error("JWT认证失败: {}", ex.getMessage(), ex);
            // 不清除SecurityContext，让后续过滤器处理
        }

        // 继续过滤器链
        filterChain.doFilter(request, response);
    }

    /**
     * 从HTTP请求中提取JWT令牌
     * 支持从Authorization头（Bearer token）或查询参数（token）中提取
     *
     * @param request HTTP请求
     * @return JWT令牌，如果未找到则返回null
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        // 1. 从Authorization头中提取
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            log.debug("从Authorization头提取JWT令牌: tokenLength={}", token.length());
            return token;
        }

        // 2. 从查询参数中提取（可选，用于某些特殊情况）
        String tokenParam = request.getParameter("token");
        if (StringUtils.hasText(tokenParam)) {
            log.debug("从查询参数提取JWT令牌: tokenLength={}", tokenParam.length());
            return tokenParam;
        }

        // 3. 从Cookie中提取（可选，用于某些特殊情况）
        // 这里可以根据需要实现Cookie提取逻辑

        log.debug("未找到JWT令牌");
        return null;
    }

    /**
     * 判断请求是否应该跳过JWT认证
     * 默认情况下，所有请求都需要认证，但可以在这里配置例外
     *
     * @param request HTTP请求
     * @return 是否跳过认证
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // 跳过认证的路径（这些路径已经在SecurityConfig中配置为permitAll）
        // 这里可以添加额外的跳过逻辑
        boolean shouldSkip = path.startsWith("/api/v1/auth/") ||
                path.startsWith("/actuator/health") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/v3/api-docs/") ||
                path.equals("/swagger-ui.html") ||
                path.startsWith("/webjars/") ||
                path.startsWith("/swagger-resources/") ||
                path.startsWith("/configuration/") ||
                path.equals("/favicon.ico") ||
                path.equals("/") ||
                path.equals("/error") ||
                // OPTIONS请求用于CORS预检
                "OPTIONS".equals(method);

        if (shouldSkip) {
            log.debug("跳过JWT认证: method={}, path={}", method, path);
        }

        return shouldSkip;
    }
}