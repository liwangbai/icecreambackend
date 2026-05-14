-- 帖子表新增游戏相关字段
ALTER TABLE posts
    ADD COLUMN faction VARCHAR(50) COMMENT '阵营',
    ADD COLUMN region VARCHAR(50) COMMENT '大区',
    ADD COLUMN server VARCHAR(100) COMMENT '服务器',
    ADD COLUMN body_type VARCHAR(50) COMMENT '体型',
    ADD COLUMN gameplay VARCHAR(50) COMMENT '玩法',
    ADD COLUMN target VARCHAR(50) COMMENT '寻找目标',
    ADD COLUMN contact_detail VARCHAR(200) COMMENT '联系方式',
    ADD COLUMN image_urls TEXT COMMENT '图片链接JSON数组',
    ADD COLUMN tags VARCHAR(500) COMMENT '标签JSON数组';
