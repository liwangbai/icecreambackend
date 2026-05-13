-- 评论系统重构迁移脚本
-- 将现有评论表升级为统一两级评论模型

-- 1. 新增 root_id 字段（用于将所有2级评论归组到同一1级评论下）
ALTER TABLE comments ADD COLUMN root_id BIGINT COMMENT '根评论ID（一级评论为null，二级评论指向其所属的一级评论）';

-- 2. 新增 reply_to_user_id 字段（用于@功能）
ALTER TABLE comments ADD COLUMN reply_to_user_id BIGINT COMMENT '回复目标用户ID（用于@功能，可为null）';

-- 3. 创建 root_id 索引（用于查询某一级评论下的所有二级评论）
CREATE INDEX idx_root_id ON comments(root_id);

-- 4. 为现有数据设置 root_id（处理历史数据）
-- 对于所有 parent_id IS NOT NULL 且 status=1 的评论，设置 root_id 为其最近的1级祖先评论ID
-- 由于原系统只支持两级，这里 parent_id IS NOT NULL 的评论的 root_id 应该等于其 parent_id
UPDATE comments SET root_id = parent_id WHERE parent_id IS NOT NULL AND status = 1;