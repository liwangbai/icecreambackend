-- MySQL数据库初始化脚本
-- 此脚本在MySQL容器首次启动时执行（如果需要）
-- 通常通过Docker volumes或entrypoint脚本加载

-- 创建示例表（仅供参考，实际表结构根据业务需求设计）
CREATE TABLE IF NOT EXISTS example_table (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '名称',
    value VARCHAR(255) COMMENT '值',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_name (name),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='示例表';

-- 创建用户表示例（实际项目中根据需求设计）
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    email VARCHAR(100) UNIQUE NOT NULL COMMENT '邮箱',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    role VARCHAR(20) DEFAULT 'USER' COMMENT '角色',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 插入示例数据（可选）
INSERT IGNORE INTO example_table (name, value) VALUES
('example1', 'value1'),
('example2', 'value2'),
('example3', 'value3');

-- 注意：实际项目中建议使用数据库迁移工具（如Flyway或Liquibase）管理数据库版本