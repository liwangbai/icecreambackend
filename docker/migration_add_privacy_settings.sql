-- 迁移脚本：用户表新增 following_visibility, follower_visibility 隐私设置字段
-- 默认值为 1（所有人可见），保持向后兼容

ALTER TABLE users
    ADD COLUMN following_visibility INT DEFAULT 1 COMMENT '关注列表可见性：0-仅自己可见，1-所有人可见' AFTER history_count,
    ADD COLUMN follower_visibility INT DEFAULT 1 COMMENT '粉丝列表可见性：0-仅自己可见，1-所有人可见' AFTER following_visibility;
