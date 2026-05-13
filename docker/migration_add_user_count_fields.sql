-- 迁移脚本：用户表新增 like_count, collection_count, history_count 字段
-- 为物化计数方案添加列并回填现有数据

-- 新增字段
ALTER TABLE users
    ADD COLUMN like_count INT DEFAULT 0 COMMENT '获赞数（帖子点赞数+评论点赞数）' AFTER following_count,
    ADD COLUMN collection_count INT DEFAULT 0 COMMENT '收藏的帖子数' AFTER like_count,
    ADD COLUMN history_count INT DEFAULT 0 COMMENT '浏览历史条数' AFTER collection_count;

-- 回填现有数据：like_count = 帖子获赞数 + 评论获赞数
UPDATE users u SET like_count = (
    COALESCE((SELECT COUNT(*) FROM post_likes pl INNER JOIN posts p ON pl.post_id = p.id WHERE p.user_id = u.id), 0)
    + COALESCE((SELECT COUNT(*) FROM comment_likes cl INNER JOIN comments c ON cl.comment_id = c.id WHERE c.user_id = u.id), 0)
);

-- 回填现有数据：collection_count = 收藏的帖子数
UPDATE users u SET collection_count = (
    COALESCE((SELECT COUNT(*) FROM post_favorites WHERE user_id = u.id), 0)
);

-- 回填现有数据：history_count = 浏览历史条数
UPDATE users u SET history_count = (
    COALESCE((SELECT COUNT(*) FROM user_browsing_history WHERE user_id = u.id), 0)
);
