package com.icecream.backend.mapper;

import com.icecream.backend.model.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * 标签数据访问接口
 * 对应数据库中的tags表（预定义标签）
 * 注意：标签管理功能通常仅管理员使用
 */
@Mapper
public interface TagMapper {

    // ========== 基本CRUD操作（仅管理员） ==========

    /**
     * 根据ID查询标签
     * @param id 标签ID
     * @return 标签信息（Optional）
     */
    Optional<Tag> findById(@Param("id") Long id);

    /**
     * 根据名称查询标签
     * @param name 标签名称
     * @return 标签信息（Optional）
     */
    Optional<Tag> findByName(@Param("name") String name);

    /**
     * 查询所有标签（仅管理员使用）
     * @return 标签列表
     */
    List<Tag> findAll();

    /**
     * 查询所有启用的标签
     * @return 启用的标签列表
     */
    List<Tag> findActiveTags();

    /**
     * 插入新标签
     * @param tag 标签对象
     * @return 影响的行数
     */
    int insert(Tag tag);

    /**
     * 更新标签信息
     * @param tag 标签对象
     * @return 影响的行数
     */
    int update(Tag tag);

    /**
     * 根据ID删除标签
     * @param id 标签ID
     * @return 影响的行数
     */
    int delete(@Param("id") Long id);

    // ========== 业务方法 ==========

    /**
     * 增加标签使用次数
     * @param tagId 标签ID
     * @return 影响的行数
     */
    int incrementUseCount(@Param("tagId") Long tagId);

    /**
     * 减少标签使用次数
     * @param tagId 标签ID
     * @return 影响的行数
     */
    int decrementUseCount(@Param("tagId") Long tagId);

    // ========== 帖子标签关联操作 ==========

    /**
     * 添加帖子标签关联
     * @param postId 帖子ID
     * @param tagId 标签ID
     * @return 影响的行数
     */
    int insertPostTag(@Param("postId") Long postId, @Param("tagId") Long tagId);

    /**
     * 根据帖子ID删除所有标签关联
     * @param postId 帖子ID
     * @return 影响的行数
     */
    int deletePostTagsByPostId(@Param("postId") Long postId);

    /**
     * 根据帖子ID查询关联的标签
     * @param postId 帖子ID
     * @return 标签列表
     */
    List<Tag> findTagsByPostId(@Param("postId") Long postId);

    // ========== 热门标签查询 ==========

    /**
     * 查询热门标签（按使用次数降序）
     * @param limit 返回数量限制
     * @return 热门标签列表
     */
    List<Tag> findPopularTags(@Param("limit") int limit);
}