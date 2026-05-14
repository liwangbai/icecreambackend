package com.icecream.backend.config;

import com.icecream.backend.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${cors.allowed-origins:*}")
    private String allowedOrigins;

    @Value("${security.require-ssl:false}")
    private boolean requireSsl;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 生产环境强制HTTPS
        if (requireSsl) {
            http.requiresChannel(channel -> channel.anyRequest().requiresSecure());
        }

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setContentType("application/json");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"message\":\"未登录或登录已过期\",\"status\":401}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setContentType("application/json");
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("{\"message\":\"没有访问权限\",\"status\":403}");
                })
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                // 公开读接口（游客可访问）
                .requestMatchers(HttpMethod.GET, "/api/v1/posts").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/posts/{postId}").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/posts/user/{userId}").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/posts/following").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/posts/hot-tags").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/posts/by-tag/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/posts/{postId}/comments").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/comments/{commentId}").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/comments/{commentId}/replies").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/comments/{commentId}/is-liked").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/tags/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/users/{userId}").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/users/search").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/users/{userId}/followers").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/users/{userId}/following").permitAll()
                .requestMatchers("/actuator/health", "/manage/health").permitAll()
                // 上传文件（图片等静态资源，公开访问）
                .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                // WebSocket端点
                .requestMatchers("/ws/**").permitAll()
                .requestMatchers("/ws").permitAll()
                // Swagger（仅在非生产环境开放，生产环境由 springdoc 配置禁用）
                .requestMatchers(
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/swagger-ui.html/**",
                        "/v3/api-docs/**",
                        "/v3/api-docs",
                        "/swagger-resources/**",
                        "/swagger-resources",
                        "/configuration/ui",
                        "/configuration/security",
                        "/webjars/**"
                ).permitAll()
                .requestMatchers("/", "/error", "/webjars/**", "/favicon.ico").permitAll()
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        if ("*".equals(allowedOrigins) || allowedOrigins.isEmpty()) {
            configuration.setAllowedOriginPatterns(List.of("*"));
        } else {
            configuration.setAllowedOriginPatterns(Arrays.asList(allowedOrigins.split(",")));
        }
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}