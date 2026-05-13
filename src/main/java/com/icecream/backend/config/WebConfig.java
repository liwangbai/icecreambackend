package com.icecream.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * 配置静态资源映射等Web相关设置
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final FileUploadProperties fileUploadProperties;

    /**
     * 配置静态资源处理器
     * 将文件上传目录映射为可通过URL访问的静态资源
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取文件上传基础目录
        String uploadDir = fileUploadProperties.getUploadDir();

        // 确保目录路径以文件分隔符结尾
        if (!uploadDir.endsWith(java.io.File.separator)) {
            uploadDir += java.io.File.separator;
        }

        // 将本地文件系统目录映射为可通过URL访问的静态资源
        // 例如：将 ./uploads/ 目录映射为 /uploads/** URL
        String resourceLocation = "file:" + uploadDir;
        String urlPattern = fileUploadProperties.getUrlPrefix() + "/**";

        registry.addResourceHandler(urlPattern)
                .addResourceLocations(resourceLocation)
                .setCachePeriod(3600) // 缓存1小时
                .resourceChain(true);

        // 同时添加classpath资源映射，用于开发环境
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600);
    }
}