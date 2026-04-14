package com.icecream.backend.util;

public class Constants {
    private Constants() {
        // 私有构造函数防止实例化
    }

    // API路径常量
    public static final String API_V1 = "/api/v1";
    public static final String AUTH_PATH = API_V1 + "/auth";
    public static final String USERS_PATH = API_V1 + "/users";
    public static final String COMMUNITIES_PATH = API_V1 + "/communities";
    public static final String POSTS_PATH = API_V1 + "/posts";

    // 分页常量
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "10";
    public static final String DEFAULT_SORT_BY = "createdAt";
    public static final String DEFAULT_SORT_DIRECTION = "desc";

    // 安全常量
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String AUTHORIZATION_HEADER = "Authorization";

    // 时间格式
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    // 文件上传常量
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    public static final String[] ALLOWED_FILE_TYPES = {"image/jpeg", "image/png", "image/gif", "application/pdf"};

    // 角色常量
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_MODERATOR = "ROLE_MODERATOR";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
}