package com.icecream.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.*;

/**
 * HTTP请求日志拦截器
 * 格式化打印收到的C端请求和返回的数据
 */
@Slf4j
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final String START_TIME_ATTR = "requestStartTime";
    private final ObjectMapper objectMapper;

    public RequestLoggingInterceptor() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME_ATTR, System.currentTimeMillis());

        // 只记录 API 请求
        String uri = request.getRequestURI();
        if (!uri.startsWith("/api/")) {
            return true;
        }

        // 记录请求参数
        Map<String, Object> requestInfo = new LinkedHashMap<>();
        requestInfo.put("method", request.getMethod());
        requestInfo.put("uri", request.getRequestURI());

        String queryString = request.getQueryString();
        if (queryString != null && !queryString.isEmpty()) {
            requestInfo.put("query", queryString);
        }

        // 获取路径参数
        @SuppressWarnings("unchecked")
        Map<String, String> pathParams = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (pathParams != null && !pathParams.isEmpty()) {
            requestInfo.put("pathParams", pathParams);
        }

        // 获取请求参数
        Map<String, String[]> paramMap = request.getParameterMap();
        if (!paramMap.isEmpty()) {
            Map<String, Object> params = new LinkedHashMap<>();
            paramMap.forEach((key, values) -> {
                if (values.length == 1) {
                    params.put(key, values[0]);
                } else {
                    params.put(key, Arrays.asList(values));
                }
            });
            requestInfo.put("params", params);
        }

        // 获取请求头（仅记录重要的）
        Map<String, String> headers = new LinkedHashMap<>();
        Collections.list(request.getHeaderNames()).forEach(name -> {
            String lowerName = name.toLowerCase();
            if (lowerName.contains("content-type") || lowerName.contains("authorization") ||
                lowerName.contains("token") || lowerName.contains("user") || lowerName.contains("device")) {
                headers.put(name, request.getHeader(name));
            }
        });
        if (!headers.isEmpty()) {
            requestInfo.put("headers", maskSensitiveHeaders(headers));
        }

        logRequest(requestInfo);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 只记录 API 请求
        String uri = request.getRequestURI();
        if (!uri.startsWith("/api/")) {
            return;
        }

        Long startTime = (Long) request.getAttribute(START_TIME_ATTR);
        long duration = startTime != null ? System.currentTimeMillis() - startTime : 0;

        Map<String, Object> responseInfo = new LinkedHashMap<>();
        responseInfo.put("uri", uri);
        responseInfo.put("status", response.getStatus());
        responseInfo.put("duration", duration + "ms");

        if (ex != null) {
            responseInfo.put("error", ex.getMessage());
        }

        logResponse(responseInfo);
    }

    private void logRequest(Map<String, Object> requestInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("╔══════════════════════════════════════════════════════════════════╗\n");
        sb.append("║  📥 [C端请求]                                                       ║\n");
        sb.append("╠══════════════════════════════════════════════════════════════════╣\n");

        String requestJson = toJson(requestInfo);
        String[] lines = requestJson.split("\n");
        for (String line : lines) {
            sb.append("║  ").append(line).append("\n");
        }

        sb.append("╚══════════════════════════════════════════════════════════════════╝");
        log.info(sb.toString());
    }

    private void logResponse(Map<String, Object> responseInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("╔══════════════════════════════════════════════════════════════════╗\n");
        sb.append("║  📤 [响应完成]                                                     ║\n");
        sb.append("╠══════════════════════════════════════════════════════════════════╣\n");

        String responseJson = toJson(responseInfo);
        String[] lines = responseJson.split("\n");
        for (String line : lines) {
            sb.append("║  ").append(line).append("\n");
        }

        sb.append("╚══════════════════════════════════════════════════════════════════╝");
        log.info(sb.toString());
    }

    private Map<String, String> maskSensitiveHeaders(Map<String, String> headers) {
        Map<String, String> masked = new LinkedHashMap<>();
        headers.forEach((key, value) -> {
            if (key.toLowerCase().contains("authorization") || key.toLowerCase().contains("token")) {
                if (value != null && value.length() > 10) {
                    masked.put(key, value.substring(0, 10) + "***");
                } else {
                    masked.put(key, "***");
                }
            } else {
                masked.put(key, value);
            }
        });
        return masked;
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            return obj.toString();
        }
    }
}