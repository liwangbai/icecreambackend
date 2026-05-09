-- MySQL数据库初始化脚本
-- 此脚本在MySQL容器首次启动时执行（如果需要）
-- 通常通过Docker volumes或entrypoint脚本加载

-- 注意：实际项目中建议使用数据库迁移工具（如Flyway或Liquibase）管理数据库版本

-- ========== 用户表 ==========
-- 创建用户表（如果不存在）
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    email VARCHAR(100) UNIQUE NOT NULL COMMENT '邮箱',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar_url VARCHAR(500) COMMENT '头像URL',
    bio TEXT COMMENT '个人简介',
    gender TINYINT DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    role VARCHAR(20) DEFAULT 'ROLE_USER' COMMENT '角色：ROLE_USER, ROLE_ADMIN等',
    last_login_at DATETIME COMMENT '最后登录时间',
    post_count INT DEFAULT 0 COMMENT '发帖数',
    follower_count INT DEFAULT 0 COMMENT '粉丝数',
    following_count INT DEFAULT 0 COMMENT '关注数',
    like_count INT DEFAULT 0 COMMENT '获赞数（帖子点赞数+评论点赞数）',
    collection_count INT DEFAULT 0 COMMENT '收藏的帖子数',
    history_count INT DEFAULT 0 COMMENT '浏览历史条数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_status (status),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ========== 帖子表 ==========
CREATE TABLE IF NOT EXISTS posts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '发布用户ID',
    title VARCHAR(200) NOT NULL COMMENT '帖子标题',
    content TEXT NOT NULL COMMENT '帖子内容',
    summary VARCHAR(500) COMMENT '摘要',
    cover_image_url VARCHAR(500) COMMENT '封面图片URL',
    view_count INT DEFAULT 0 COMMENT '浏览数',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    favorite_count INT DEFAULT 0 COMMENT '收藏数',
    comment_count INT DEFAULT 0 COMMENT '评论数',
    status TINYINT DEFAULT 1 COMMENT '状态：0-草稿，1-已发布，2-已删除',
    visibility TINYINT DEFAULT 1 COMMENT '可见性：0-私密，1-公开',
    is_top BOOLEAN DEFAULT FALSE COMMENT '是否置顶',
    published_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_status_visibility (status, visibility),
    INDEX idx_published_at (published_at DESC),
    INDEX idx_is_top_published_at (is_top, published_at DESC),
    INDEX idx_view_count (view_count DESC),
    INDEX idx_like_count (like_count DESC)
    FULLTEXT INDEX ft_title_content (title, content) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子表';

-- ========== 标签表（预定义标签） ==========
CREATE TABLE IF NOT EXISTS tags (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) UNIQUE NOT NULL COMMENT '标签名称',
    description VARCHAR(200) COMMENT '标签描述',
    color VARCHAR(20) DEFAULT '#1890ff' COMMENT '标签颜色',
    icon VARCHAR(100) COMMENT '标签图标',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    use_count INT DEFAULT 0 COMMENT '使用次数',
    sort_order INT DEFAULT 0 COMMENT '排序顺序',
    created_by BIGINT COMMENT '创建人ID（管理员）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_name (name),
    INDEX idx_is_active_sort_order (is_active, sort_order),
    INDEX idx_use_count (use_count DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预定义标签表';

-- ========== 帖子标签关联表 ==========
CREATE TABLE IF NOT EXISTS post_tags (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT NOT NULL COMMENT '帖子ID',
    tag_id BIGINT NOT NULL COMMENT '标签ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_post_tag (post_id, tag_id),
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE,
    INDEX idx_post_id (post_id),
    INDEX idx_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子标签关联表';

-- ========== 用户关注表（单向关注） ==========
CREATE TABLE IF NOT EXISTS user_follows (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    follower_id BIGINT NOT NULL COMMENT '关注者ID',
    following_id BIGINT NOT NULL COMMENT '被关注者ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
    UNIQUE KEY uk_follower_following (follower_id, following_id),
    FOREIGN KEY (follower_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (following_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_follower_id (follower_id),
    INDEX idx_following_id (following_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户单向关注关系表';

-- ========== 帖子收藏表 ==========
CREATE TABLE IF NOT EXISTS post_favorites (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    post_id BIGINT NOT NULL COMMENT '帖子ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    UNIQUE KEY uk_user_post_fav (user_id, post_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_post_id (post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子收藏表';

-- ========== 用户浏览历史表 ==========
CREATE TABLE IF NOT EXISTS user_browsing_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    post_id BIGINT NOT NULL COMMENT '帖子ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
    UNIQUE KEY uk_user_post_history (user_id, post_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户浏览历史表';

-- ========== 帖子点赞表 ==========
CREATE TABLE IF NOT EXISTS post_likes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    post_id BIGINT NOT NULL COMMENT '帖子ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    UNIQUE KEY uk_user_post (user_id, post_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_post_id (post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子点赞表';

-- ========== 评论表 ==========
CREATE TABLE IF NOT EXISTS comments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT NOT NULL COMMENT '所属帖子ID',
    user_id BIGINT NOT NULL COMMENT '评论者用户ID',
    content TEXT NOT NULL COMMENT '评论内容',
    parent_id BIGINT COMMENT '父评论ID（顶级为null，支持楼中楼）',
    root_id BIGINT COMMENT '根评论ID（一级评论为null，二级评论指向其所属的一级评论）',
    reply_to_user_id BIGINT COMMENT '回复目标用户ID（用于@功能，可为null）',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    reply_count INT DEFAULT 0 COMMENT '回复数（仅一级评论显示）',
    status TINYINT DEFAULT 1 COMMENT '状态：0-已删除，1-正常',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES comments(id) ON DELETE CASCADE,
    INDEX idx_post_id (post_id),
    INDEX idx_user_id (user_id),
    INDEX idx_parent_id (parent_id),
    INDEX idx_root_id (root_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- ========== 评论点赞表 ==========
CREATE TABLE IF NOT EXISTS comment_likes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    comment_id BIGINT NOT NULL COMMENT '评论ID',
    user_id BIGINT NOT NULL COMMENT '点赞用户ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    UNIQUE KEY uk_comment_user (comment_id, user_id),
    FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论点赞表';

-- ========== 示例数据 ==========

-- 插入示例用户（密码均为：password123，BCrypt加密后）
INSERT IGNORE INTO users (username, email, password_hash, nickname, role) VALUES
('admin', 'admin@icecream.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTV6UiC', '系统管理员', 'ROLE_ADMIN'),
('user1', 'user1@icecream.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTV6UiC', '冰淇淋爱好者', 'ROLE_USER'),
('user2', 'user2@icecream.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTV6UiC', '甜品达人', 'ROLE_USER');

-- 插入预定义标签（由管理员创建）
INSERT IGNORE INTO tags (name, description, color, icon, is_active, sort_order, created_by) VALUES
('美食', '分享美食体验', '#ff4d4f', 'food', TRUE, 1, 1),
('旅行', '旅行见闻分享', '#52c41a', 'travel', TRUE, 2, 1),
('摄影', '摄影作品展示', '#1890ff', 'camera', TRUE, 3, 1),
('生活', '日常生活记录', '#722ed1', 'home', TRUE, 4, 1),
('科技', '科技产品讨论', '#13c2c2', 'tech', TRUE, 5, 1),
('娱乐', '娱乐休闲话题', '#eb2f96', 'entertainment', TRUE, 6, 1),
('运动', '运动健身分享', '#fa8c16', 'sports', TRUE, 7, 1),
('学习', '学习经验交流', '#fadb14', 'study', TRUE, 8, 1);

-- 插入示例帖子
INSERT IGNORE INTO posts (user_id, title, content, summary, status, visibility) VALUES
(2, '第一次尝试自制冰淇淋', '今天尝试了自制香草冰淇淋，味道非常棒！需要的材料有...', '分享自制冰淇淋的经验和配方', 1, 1),
(3, '周末的甜品店探店', '发现了一家超赞的甜品店，他们的招牌冰淇淋简直完美...', '探店分享：城市中的隐藏甜品宝藏', 1, 1),
(2, '摄影技巧分享：如何拍出美味的食物', '拍摄食物照片时，光线和角度非常重要...', '食物摄影技巧入门指南', 1, 1);

-- 插入帖子标签关联
INSERT IGNORE INTO post_tags (post_id, tag_id) VALUES
(1, 1), -- 帖子1 - 美食
(1, 4), -- 帖子1 - 生活
(2, 1), -- 帖子2 - 美食
(2, 4), -- 帖子2 - 生活
(3, 3), -- 帖子3 - 摄影
(3, 1); -- 帖子3 - 美食

-- 插入关注关系
INSERT IGNORE INTO user_follows (follower_id, following_id) VALUES
(2, 3), -- 用户2关注用户3
(3, 2); -- 用户3关注用户2

-- 插入点赞记录
INSERT IGNORE INTO post_likes (user_id, post_id) VALUES
(2, 2), -- 用户2点赞帖子2
(3, 1); -- 用户3点赞帖子1

-- 更新统计信息（使用示例数据后的统计）
UPDATE users SET post_count = (SELECT COUNT(*) FROM posts WHERE user_id = users.id);
UPDATE tags SET use_count = (SELECT COUNT(*) FROM post_tags WHERE tag_id = tags.id);
UPDATE posts SET view_count = FLOOR(RAND() * 100) + 10,
                 like_count = (SELECT COUNT(*) FROM post_likes WHERE post_id = posts.id),
                 comment_count = FLOOR(RAND() * 20);

-- 示例表（保留原有结构，可选）
CREATE TABLE IF NOT EXISTS example_table (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '名称',
    value VARCHAR(255) COMMENT '值',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_name (name),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='示例表';

INSERT IGNORE INTO example_table (name, value) VALUES
('example1', 'value1'),
('example2', 'value2'),
('example3', 'value3');

-- ========== 会话表（私聊） ==========
CREATE TABLE IF NOT EXISTS conversations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user1_id BIGINT NOT NULL COMMENT '用户1ID（较小者）',
    user2_id BIGINT NOT NULL COMMENT '用户2ID（较大者）',
    last_message_id BIGINT COMMENT '最后一条消息ID',
    last_message_at DATETIME COMMENT '最后消息时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_pair (user1_id, user2_id),
    INDEX idx_user1_id (user1_id),
    INDEX idx_user2_id (user2_id),
    INDEX idx_last_message_at (last_message_at DESC),
    FOREIGN KEY (user1_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (user2_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话表（1v1私聊）';

-- ========== 消息表 ==========
CREATE TABLE IF NOT EXISTS messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    conversation_id BIGINT NOT NULL COMMENT '所属会话ID',
    sender_id BIGINT NOT NULL COMMENT '发送者ID',
    content TEXT NOT NULL COMMENT '消息内容',
    type TINYINT DEFAULT 1 COMMENT '消息类型：1-文本，2-图片，3-系统消息',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常，0-已撤回',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_conversation_id (conversation_id),
    INDEX idx_sender_id (sender_id),
    INDEX idx_created_at (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';

-- ========== 消息未读表 ==========
CREATE TABLE IF NOT EXISTS message_reads (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    conversation_id BIGINT NOT NULL COMMENT '会话ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    last_read_message_id BIGINT COMMENT '最后已读消息ID',
    last_read_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '最后已读时间',
    UNIQUE KEY uk_conversation_user (conversation_id, user_id),
    FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息已读状态表';