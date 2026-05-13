package com.icecream.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.icecream.backend.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.*;

/**
 * 响应日志切面
 * 格式化打印返回给C端的响应数据
 */
@Slf4j
@Component
@RestControllerAdvice
public class ResponseLoggingAdvice implements ResponseBodyAdvice<Object> {

    private static final int MAX_LOG_SIZE = 3072;
    private final ObjectMapper objectMapper;

    public ResponseLoggingAdvice() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> converterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {

        // 只记录 API 请求
        String uri = request.getURI().getPath();
        if (!uri.startsWith("/api/")) {
            return body;
        }

        // 获取请求信息
        Map<String, Object> logInfo = new LinkedHashMap<>();
        logInfo.put("uri", uri);

        // 获取响应数据
        if (body != null) {
            if (body instanceof ApiResponse) {
                ApiResponse<?> apiResponse = (ApiResponse<?>) body;
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("success", apiResponse.isSuccess());
                data.put("message", apiResponse.getMessage());
                if (apiResponse.getData() != null) {
                    data.put("data", apiResponse.getData());
                }
                logInfo.put("response", data);
            } else {
                logInfo.put("response", body);
            }
        } else {
            logInfo.put("response", "null");
        }

        logResponse(logInfo);
        return body;
    }

    private void logResponse(Map<String, Object> logInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("╔══════════════════════════════════════════════════════════════════╗\n");
        sb.append("║  📤 [响应数据]                                                      ║\n");
        sb.append("╠══════════════════════════════════════════════════════════════════╣\n");

        String json = toJson(logInfo);
        // 美化JSON格式
        String[] lines = json.split("\n");
        for (String line : lines) {
            sb.append("║  ").append(line).append("\n");
        }

        sb.append("╚══════════════════════════════════════════════════════════════════╝");
        log.info(sb.toString());
    }

    private String toJson(Object obj) {
        try {
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
            if (json.length() > MAX_LOG_SIZE) {
                json = json.substring(0, MAX_LOG_SIZE) + "\n  ...(truncated)";
            }
            return json;
        } catch (Exception e) {
            return obj.toString();
        }
    }
}