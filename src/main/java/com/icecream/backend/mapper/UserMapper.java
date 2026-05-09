package com.icecream.backend.mapper;

import com.icecream.backend.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问接口
 * 对应数据库中的users表
 */
@Mapper
public interface UserMapper {

    // ========== 基本CRUD操作 ==========

    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return 用户信息（Optional）
     */
    Optional<User> findById(@Param("id") Long id);

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户信息（Optional）
     */
    Optional<User> findByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询用户
     * @param email 邮箱地址
     * @return 用户信息（Optional）
     */
    Optional<User> findByEmail(@Param("email") String email);

    /**
     * 查询所有用户（仅管理员使用）
     * @return 用户列表
     */
    List<User> findAll();

    /**
     * 插入新用户
     * @param user 用户对象
     * @return 影响的行数
     */
    int insert(User user);

    /**
     * 更新用户信息
     * @param user 用户对象
     * @return 影响的行数
     */
    int update(User user);

    /**
     * 根据ID删除用户（仅管理员使用）
     * @param id 用户ID
     * @return 影响的行数
     */
    int delete(@Param("id") Long id);

    // ========== 业务方法 ==========

    /**
     * 增加用户的发帖数
     * @param userId 用户ID
     * @return 影响的行数
     */
    int incrementPostCount(@Param("userId") Long userId);

    /**
     * 增加用户的粉丝数
     * @param userId 用户ID
     * @return 影响的行数
     */
    int incrementFollowerCount(@Param("userId") Long userId);

    /**
     * 增加用户的关注数
     * @param userId 用户ID
     * @return 影响的行数
     */
    int incrementFollowingCount(@Param("userId") Long userId);

    /**
     * 减少用户的粉丝数
     * @param userId 用户ID
     * @return 影响的行数
     */
    int decrementFollowerCount(@Param("userId") Long userId);

    /**
     * 减少用户的关注数
     * @param userId 用户ID
     * @return 影响的行数
     */
    int decrementFollowingCount(@Param("userId") Long userId);

    /**
     * 减少用户的发帖数
     * @param userId 用户ID
     * @return 影响的行数
     */
    int decrementPostCount(@Param("userId") Long userId);

    /**
     * 增加用户的获赞数
     * @param userId 用户ID
     * @return 影响的行数
     */
    int incrementLikeCount(@Param("userId") Long userId);

    /**
     * 减少用户的获赞数
     * @param userId 用户ID
     * @return 影响的行数
     */
    int decrementLikeCount(@Param("userId") Long userId);

    /**
     * 增加用户的收藏数
     * @param userId 用户ID
     * @return 影响的行数
     */
    int incrementCollectionCount(@Param("userId") Long userId);

    /**
     * 减少用户的收藏数
     * @param userId 用户ID
     * @return 影响的行数
     */
    int decrementCollectionCount(@Param("userId") Long userId);

    /**
     * 增加用户的浏览历史条数
     * @param userId 用户ID
     * @return 影响的行数
     */
    int incrementHistoryCount(@Param("userId") Long userId);

    /**
     * 减少用户的浏览历史条数
     * @param userId 用户ID
     * @return 影响的行数
     */
    int decrementHistoryCount(@Param("userId") Long userId);

    /**
     * 更新用户最后登录时间
     * @param userId 用户ID
     * @return 影响的行数
     */
    int updateLastLogin(@Param("userId") Long userId);

    // ========== 关注相关操作 ==========

    /**
     * 检查关注关系是否存在
     * @param followerId 关注者ID
     * @param followingId 被关注者ID
     * @return 是否存在关注关系
     */
    boolean existsFollow(@Param("followerId") Long followerId, @Param("followingId") Long followingId);

    /**
     * 添加关注关系
     * @param followerId 关注者ID
     * @param followingId 被关注者ID
     * @return 影响的行数
     */
    int insertFollow(@Param("followerId") Long followerId, @Param("followingId") Long followingId);

    /**
     * 删除关注关系
     * @param followerId 关注者ID
     * @param followingId 被关注者ID
     * @return 影响的行数
     */
    int deleteFollow(@Param("followerId") Long followerId, @Param("followingId") Long followingId);

    /**
     * 查询用户的粉丝列表
     * @param userId 用户ID
     * @return 粉丝用户列表
     */
    List<User> findFollowers(@Param("userId") Long userId);

    /**
     * 查询用户的关注列表
     * @param userId 用户ID
     * @return 关注用户列表
     */
    List<User> findFollowing(@Param("userId") Long userId);

    /**
     * 根据用户名或邮箱查询用户（用于登录）
     * @param usernameOrEmail 用户名或邮箱
     * @return 用户信息（Optional）
     */
    Optional<User> findByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);

    /**
     * 查询粉丝列表（带是否回关标志）
     * @param userId 当前用户ID
     * @return 关注通知列表
     */
    List<com.icecream.backend.dto.response.FollowNotification> findFollowersWithStatus(@Param("userId") Long userId);

    /**
     * 统计粉丝数量
     * @param userId 当前用户ID
     * @return 粉丝数量
     */
    long countFollowersWithStatus(@Param("userId") Long userId);
}