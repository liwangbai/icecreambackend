package com.icecream.backend.service.impl;

import com.icecream.backend.dto.request.UserUpdateRequest;
import com.icecream.backend.mapper.UserMapper;
import com.icecream.backend.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 用户服务单元测试类
 * 测试UserServiceImpl的各种用户管理场景
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("用户服务测试")
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private User anotherUser;
    private UserUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        // 初始化测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("encodedPassword");
        testUser.setNickname("Test User");
        testUser.setAvatarUrl("avatar.jpg");
        testUser.setBio("This is a test user");
        testUser.setGender(1);
        testUser.setStatus(1);
        testUser.setRole("ROLE_USER");
        testUser.setPostCount(10);
        testUser.setFollowerCount(100);
        testUser.setFollowingCount(50);

        // 初始化另一个用户
        anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPasswordHash("encodedPassword2");
        anotherUser.setNickname("Another User");
        anotherUser.setAvatarUrl("avatar2.jpg");
        anotherUser.setBio("Another test user");
        anotherUser.setGender(2);
        anotherUser.setStatus(1);
        anotherUser.setRole("ROLE_USER");
        anotherUser.setPostCount(5);
        anotherUser.setFollowerCount(50);
        anotherUser.setFollowingCount(30);

        // 初始化更新请求
        updateRequest = new UserUpdateRequest();
        updateRequest.setNickname("Updated Nickname");
        updateRequest.setBio("Updated bio");
        updateRequest.setGender(2);
        updateRequest.setAvatarUrl("new-avatar.jpg");
        updateRequest.setEmail("updated@example.com");
    }

    @Nested
    @DisplayName("获取用户信息测试")
    class GetUserTests {

        @Test
        @DisplayName("成功获取当前用户信息")
        void testGetCurrentUser_Success() {
            // Arrange
            when(userMapper.findById(1L)).thenReturn(Optional.of(testUser));

            // Act
            User result = userService.getCurrentUser(1L);

            // Assert
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("testuser", result.getUsername());
            assertEquals("Test User", result.getNickname());
            assertEquals("encodedPassword", result.getPasswordHash()); // 密码哈希应该保留

            verify(userMapper).findById(1L);
        }

        @Test
        @DisplayName("获取当前用户时用户不存在")
        void testGetCurrentUser_UserNotFound() {
            // Arrange
            when(userMapper.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> userService.getCurrentUser(999L));

            assertEquals("用户不存在: 999", exception.getMessage());
            verify(userMapper).findById(999L);
        }

        @Test
        @DisplayName("成功获取其他用户公开信息")
        void testGetUserById_Success() {
            // Arrange
            when(userMapper.findById(2L)).thenReturn(Optional.of(anotherUser));

            // Act
            User result = userService.getUserById(2L);

            // Assert
            assertNotNull(result);
            assertEquals(2L, result.getId());
            assertEquals("anotheruser", result.getUsername());
            assertEquals("Another User", result.getNickname());
            assertNull(result.getPasswordHash()); // 密码哈希应该被移除

            verify(userMapper).findById(2L);
        }

        @Test
        @DisplayName("获取其他用户时用户不存在")
        void testGetUserById_UserNotFound() {
            // Arrange
            when(userMapper.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> userService.getUserById(999L));

            assertEquals("用户不存在: 999", exception.getMessage());
            verify(userMapper).findById(999L);
        }
    }

    @Nested
    @DisplayName("更新用户信息测试")
    class UpdateUserTests {

        @Test
        @DisplayName("成功更新用户信息")
        void testUpdateUser_Success() {
            // Arrange
            when(userMapper.findById(1L)).thenReturn(Optional.of(testUser));
            when(userMapper.findByEmail("updated@example.com")).thenReturn(Optional.empty());
            when(userMapper.update(any(User.class))).thenReturn(1);

            // 模拟getUserById的行为
            User updatedUser = new User();
            updatedUser.setId(1L);
            updatedUser.setUsername("testuser");
            updatedUser.setEmail("updated@example.com");
            updatedUser.setNickname("Updated Nickname");
            updatedUser.setBio("Updated bio");
            updatedUser.setGender(2);
            updatedUser.setAvatarUrl("new-avatar.jpg");
            updatedUser.setPasswordHash(null); // 公开信息中密码哈希为null

            when(userMapper.findById(1L)).thenReturn(Optional.of(updatedUser));

            // Act
            User result = userService.updateUser(1L, updateRequest);

            // Assert
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("Updated Nickname", result.getNickname());
            assertEquals("Updated bio", result.getBio());
            assertEquals(2, result.getGender());
            assertEquals("new-avatar.jpg", result.getAvatarUrl());
            assertEquals("updated@example.com", result.getEmail());
            assertNull(result.getPasswordHash()); // 密码哈希应该被移除

            verify(userMapper, times(2)).findById(1L);
            verify(userMapper).findByEmail("updated@example.com");
            verify(userMapper).update(any(User.class));
        }

        @Test
        @DisplayName("更新用户时用户不存在")
        void testUpdateUser_UserNotFound() {
            // Arrange
            when(userMapper.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> userService.updateUser(999L, updateRequest));

            assertEquals("用户不存在: 999", exception.getMessage());
            verify(userMapper).findById(999L);
            verify(userMapper, never()).findByEmail(anyString());
            verify(userMapper, never()).update(any(User.class));
        }

        @Test
        @DisplayName("更新邮箱时邮箱已被其他用户使用")
        void testUpdateUser_EmailAlreadyUsed() {
            // Arrange
            when(userMapper.findById(1L)).thenReturn(Optional.of(testUser));
            when(userMapper.findByEmail("updated@example.com")).thenReturn(Optional.of(anotherUser));

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> userService.updateUser(1L, updateRequest));

            assertEquals("邮箱已被其他用户使用", exception.getMessage());
            verify(userMapper).findById(1L);
            verify(userMapper).findByEmail("updated@example.com");
            verify(userMapper, never()).update(any(User.class));
        }

        @Test
        @DisplayName("更新邮箱时邮箱未改变")
        void testUpdateUser_EmailNotChanged() {
            // Arrange
            updateRequest.setEmail("test@example.com"); // 与原邮箱相同
            when(userMapper.findById(1L)).thenReturn(Optional.of(testUser));
            when(userMapper.update(any(User.class))).thenReturn(1);

            // 模拟getUserById的行为
            User updatedUser = new User();
            updatedUser.setId(1L);
            updatedUser.setUsername("testuser");
            updatedUser.setEmail("test@example.com");
            updatedUser.setNickname("Updated Nickname");
            updatedUser.setBio("Updated bio");
            updatedUser.setGender(2);
            updatedUser.setAvatarUrl("new-avatar.jpg");
            updatedUser.setPasswordHash(null);

            when(userMapper.findById(1L)).thenReturn(Optional.of(updatedUser));

            // Act
            User result = userService.updateUser(1L, updateRequest);

            // Assert
            assertNotNull(result);
            assertEquals("test@example.com", result.getEmail()); // 邮箱保持不变

            verify(userMapper, times(2)).findById(1L);
            verify(userMapper, never()).findByEmail(anyString()); // 不应该检查邮箱是否被使用
            verify(userMapper).update(any(User.class));
        }

        @Test
        @DisplayName("部分更新用户信息")
        void testUpdateUser_PartialUpdate() {
            // Arrange
            UserUpdateRequest partialRequest = new UserUpdateRequest();
            partialRequest.setNickname("New Nickname Only");

            when(userMapper.findById(1L)).thenReturn(Optional.of(testUser));
            when(userMapper.update(any(User.class))).thenReturn(1);

            // 模拟getUserById的行为
            User updatedUser = new User();
            updatedUser.setId(1L);
            updatedUser.setUsername("testuser");
            updatedUser.setEmail("test@example.com");
            updatedUser.setNickname("New Nickname Only");
            updatedUser.setBio("This is a test user"); // 保持不变
            updatedUser.setGender(1); // 保持不变
            updatedUser.setAvatarUrl("avatar.jpg"); // 保持不变
            updatedUser.setPasswordHash(null);

            when(userMapper.findById(1L)).thenReturn(Optional.of(updatedUser));

            // Act
            User result = userService.updateUser(1L, partialRequest);

            // Assert
            assertNotNull(result);
            assertEquals("New Nickname Only", result.getNickname());
            assertEquals("This is a test user", result.getBio()); // 保持不变
            assertEquals(1, result.getGender()); // 保持不变
            assertEquals("avatar.jpg", result.getAvatarUrl()); // 保持不变

            verify(userMapper, times(2)).findById(1L);
            verify(userMapper, never()).findByEmail(anyString());
            verify(userMapper).update(any(User.class));
        }
    }

    @Nested
    @DisplayName("关注功能测试")
    class FollowTests {

        @Test
        @DisplayName("成功关注用户")
        void testFollowUser_Success() {
            // Arrange
            when(userMapper.findById(1L)).thenReturn(Optional.of(testUser));
            when(userMapper.findById(2L)).thenReturn(Optional.of(anotherUser));
            when(userMapper.existsFollow(1L, 2L)).thenReturn(false);
            when(userMapper.insertFollow(1L, 2L)).thenReturn(1);
            when(userMapper.incrementFollowingCount(1L)).thenReturn(1);
            when(userMapper.incrementFollowerCount(2L)).thenReturn(1);

            // Act
            userService.followUser(1L, 2L);

            // Assert
            verify(userMapper).findById(1L);
            verify(userMapper).findById(2L);
            verify(userMapper).existsFollow(1L, 2L);
            verify(userMapper).insertFollow(1L, 2L);
            verify(userMapper).incrementFollowingCount(1L);
            verify(userMapper).incrementFollowerCount(2L);
        }

        @Test
        @DisplayName("关注自己")
        void testFollowUser_SelfFollow() {
            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> userService.followUser(1L, 1L));

            assertEquals("不能关注自己", exception.getMessage());
            verify(userMapper, never()).findById(anyLong());
            verify(userMapper, never()).existsFollow(anyLong(), anyLong());
        }

        @Test
        @DisplayName("关注不存在的用户")
        void testFollowUser_UserNotFound() {
            // Arrange
            when(userMapper.findById(1L)).thenReturn(Optional.of(testUser));
            when(userMapper.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> userService.followUser(1L, 999L));

            assertEquals("用户不存在", exception.getMessage());
            verify(userMapper).findById(1L);
            verify(userMapper).findById(999L);
            verify(userMapper, never()).existsFollow(anyLong(), anyLong());
        }

        @Test
        @DisplayName("重复关注")
        void testFollowUser_AlreadyFollowing() {
            // Arrange
            when(userMapper.findById(1L)).thenReturn(Optional.of(testUser));
            when(userMapper.findById(2L)).thenReturn(Optional.of(anotherUser));
            when(userMapper.existsFollow(1L, 2L)).thenReturn(true);

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> userService.followUser(1L, 2L));

            assertEquals("已经关注了该用户", exception.getMessage());
            verify(userMapper).findById(1L);
            verify(userMapper).findById(2L);
            verify(userMapper).existsFollow(1L, 2L);
            verify(userMapper, never()).insertFollow(anyLong(), anyLong());
        }

        @Test
        @DisplayName("成功取消关注")
        void testUnfollowUser_Success() {
            // Arrange
            when(userMapper.existsFollow(1L, 2L)).thenReturn(true);
            when(userMapper.deleteFollow(1L, 2L)).thenReturn(1);
            when(userMapper.decrementFollowingCount(1L)).thenReturn(1);
            when(userMapper.decrementFollowerCount(2L)).thenReturn(1);

            // Act
            userService.unfollowUser(1L, 2L);

            // Assert
            verify(userMapper).existsFollow(1L, 2L);
            verify(userMapper).deleteFollow(1L, 2L);
            verify(userMapper).decrementFollowingCount(1L);
            verify(userMapper).decrementFollowerCount(2L);
        }

        @Test
        @DisplayName("取消未关注的用户")
        void testUnfollowUser_NotFollowing() {
            // Arrange
            when(userMapper.existsFollow(1L, 2L)).thenReturn(false);

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> userService.unfollowUser(1L, 2L));

            assertEquals("尚未关注该用户", exception.getMessage());
            verify(userMapper).existsFollow(1L, 2L);
            verify(userMapper, never()).deleteFollow(anyLong(), anyLong());
        }

        @Test
        @DisplayName("检查关注关系")
        void testIsFollowing() {
            // Arrange
            when(userMapper.existsFollow(1L, 2L)).thenReturn(true);
            when(userMapper.existsFollow(2L, 1L)).thenReturn(false);

            // Act & Assert
            assertTrue(userService.isFollowing(1L, 2L));
            assertFalse(userService.isFollowing(2L, 1L));

            verify(userMapper).existsFollow(1L, 2L);
            verify(userMapper).existsFollow(2L, 1L);
        }
    }

    @Nested
    @DisplayName("粉丝和关注列表测试")
    class FollowerFollowingTests {

        @Test
        @DisplayName("成功获取粉丝列表")
        void testGetFollowers_Success() {
            // Arrange
            List<User> followers = Arrays.asList(anotherUser);
            when(userMapper.findFollowers(1L)).thenReturn(followers);

            // Act
            List<User> result = userService.getFollowers(1L);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(2L, result.get(0).getId());
            assertEquals("anotheruser", result.get(0).getUsername());
            assertNull(result.get(0).getPasswordHash()); // 密码哈希应该被移除

            verify(userMapper).findFollowers(1L);
        }

        @Test
        @DisplayName("成功获取关注列表")
        void testGetFollowing_Success() {
            // Arrange
            List<User> following = Arrays.asList(anotherUser);
            when(userMapper.findFollowing(1L)).thenReturn(following);

            // Act
            List<User> result = userService.getFollowing(1L);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(2L, result.get(0).getId());
            assertEquals("anotheruser", result.get(0).getUsername());
            assertNull(result.get(0).getPasswordHash()); // 密码哈希应该被移除

            verify(userMapper).findFollowing(1L);
        }

        @Test
        @DisplayName("获取空粉丝列表")
        void testGetFollowers_EmptyList() {
            // Arrange
            when(userMapper.findFollowers(1L)).thenReturn(Arrays.asList());

            // Act
            List<User> result = userService.getFollowers(1L);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(userMapper).findFollowers(1L);
        }

        @Test
        @DisplayName("获取空关注列表")
        void testGetFollowing_EmptyList() {
            // Arrange
            when(userMapper.findFollowing(1L)).thenReturn(Arrays.asList());

            // Act
            List<User> result = userService.getFollowing(1L);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(userMapper).findFollowing(1L);
        }
    }

    @Nested
    @DisplayName("其他功能测试")
    class OtherTests {

        @Test
        @DisplayName("更新最后登录时间")
        void testUpdateLastLogin() {
            // Arrange
            when(userMapper.updateLastLogin(1L)).thenReturn(1);

            // Act
            userService.updateLastLogin(1L);

            // Assert
            verify(userMapper).updateLastLogin(1L);
        }
    }
}