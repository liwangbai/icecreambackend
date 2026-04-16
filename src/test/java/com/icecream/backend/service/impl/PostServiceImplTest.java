package com.icecream.backend.service.impl;

import com.icecream.backend.dto.request.PostCreateRequest;
import com.icecream.backend.dto.request.PostQueryRequest;
import com.icecream.backend.dto.request.PostUpdateRequest;
import com.icecream.backend.mapper.PostMapper;
import com.icecream.backend.mapper.TagMapper;
import com.icecream.backend.mapper.UserMapper;
import com.icecream.backend.model.Post;
import com.icecream.backend.model.Tag;
import com.icecream.backend.model.User;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 帖子服务单元测试类
 * 测试PostServiceImpl的各种帖子管理场景
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("帖子服务测试")
class PostServiceImplTest {

    @Mock
    private PostMapper postMapper;

    @Mock
    private TagMapper tagMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private PostServiceImpl postService;

    private Post testPost;
    private User testUser;
    private Tag testTag;
    private PostCreateRequest createRequest;
    private PostUpdateRequest updateRequest;
    private PostQueryRequest queryRequest;

    @BeforeEach
    void setUp() {
        // 初始化测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setNickname("Test User");
        testUser.setAvatarUrl("avatar.jpg");

        // 初始化测试标签
        testTag = new Tag();
        testTag.setId(1L);
        testTag.setName("Java");
        testTag.setDescription("Java programming");
        testTag.setIsActive(true);
        testTag.setUseCount(10);

        // 初始化测试帖子
        testPost = new Post();
        testPost.setId(1L);
        testPost.setUserId(1L);
        testPost.setTitle("Test Post Title");
        testPost.setContent("Test post content");
        testPost.setSummary("Test summary");
        testPost.setCoverImageUrl("cover.jpg");
        testPost.setStatus(1);
        testPost.setVisibility(1);
        testPost.setIsTop(false);
        testPost.setViewCount(100);
        testPost.setLikeCount(50);
        testPost.setCommentCount(20);
        testPost.setPublishedAt(LocalDateTime.now().minusDays(1));

        // 初始化创建请求
        createRequest = new PostCreateRequest();
        createRequest.setTitle("New Post Title");
        createRequest.setContent("New post content");
        createRequest.setSummary("New summary");
        createRequest.setCoverImageUrl("new-cover.jpg");
        createRequest.setStatus(1);
        createRequest.setVisibility(1);
        createRequest.setIsTop(false);
        createRequest.setTagIds(Arrays.asList(1L, 2L));

        // 初始化更新请求
        updateRequest = new PostUpdateRequest();
        updateRequest.setTitle("Updated Title");
        updateRequest.setContent("Updated content");
        updateRequest.setSummary("Updated summary");
        updateRequest.setCoverImageUrl("updated-cover.jpg");
        updateRequest.setStatus(2);
        updateRequest.setVisibility(2);
        updateRequest.setIsTop(true);
        updateRequest.setTagIds(Arrays.asList(1L));

        // 初始化查询请求
        queryRequest = new PostQueryRequest();
        queryRequest.setPage(1);
        queryRequest.setSize(10);
        queryRequest.setCurrentUserId(1L);
    }

    @Nested
    @DisplayName("创建帖子测试")
    class CreatePostTests {

        @Test
        @DisplayName("成功创建帖子")
        void testCreatePost_Success() {
            // Arrange
            Tag tag1 = new Tag();
            tag1.setId(1L);
            tag1.setIsActive(true);

            Tag tag2 = new Tag();
            tag2.setId(2L);
            tag2.setIsActive(true);

            when(tagMapper.findById(1L)).thenReturn(Optional.of(tag1));
            when(tagMapper.findById(2L)).thenReturn(Optional.of(tag2));
            when(postMapper.insert(any(Post.class))).thenAnswer(invocation -> {
                Post post = invocation.getArgument(0);
                post.setId(2L); // 模拟数据库生成ID
                return 1;
            });
            when(tagMapper.insertPostTag(anyLong(), anyLong())).thenReturn(1);
            when(tagMapper.incrementUseCount(anyLong())).thenReturn(1);
            when(userMapper.incrementPostCount(1L)).thenReturn(1);

            // 模拟getPostById的行为
            Post createdPost = new Post();
            createdPost.setId(2L);
            createdPost.setUserId(1L);
            createdPost.setTitle("New Post Title");
            createdPost.setContent("New post content");
            createdPost.setSummary("New summary");
            createdPost.setCoverImageUrl("new-cover.jpg");
            createdPost.setStatus(1);
            createdPost.setVisibility(1);
            createdPost.setIsTop(false);
            createdPost.setViewCount(0);
            createdPost.setLikeCount(0);
            createdPost.setCommentCount(0);
            createdPost.setPublishedAt(LocalDateTime.now());
            createdPost.setAuthor(testUser);
            createdPost.setTags(Arrays.asList(tag1, tag2));
            createdPost.setLiked(false);

            when(postMapper.findById(2L)).thenReturn(Optional.of(createdPost));
            when(userMapper.findById(1L)).thenReturn(Optional.of(testUser));
            when(tagMapper.findTagsByPostId(2L)).thenReturn(Arrays.asList(tag1, tag2));
            when(postMapper.existsLike(1L, 2L)).thenReturn(false);
            when(postMapper.incrementViewCount(2L)).thenReturn(1);

            // Act
            Post result = postService.createPost(1L, createRequest);

            // Assert
            assertNotNull(result);
            assertEquals(2L, result.getId());
            assertEquals("New Post Title", result.getTitle());
            assertEquals("New post content", result.getContent());
            assertEquals(testUser, result.getAuthor());
            assertEquals(2, result.getTags().size());
            assertFalse(result.isLiked());

            verify(tagMapper, times(2)).findById(anyLong());
            verify(postMapper).insert(any(Post.class));
            verify(tagMapper, times(2)).insertPostTag(anyLong(), anyLong());
            verify(tagMapper, times(2)).incrementUseCount(anyLong());
            verify(userMapper).incrementPostCount(1L);
            verify(postMapper).findById(2L);
            verify(postMapper).incrementViewCount(2L);
        }

        @Test
        @DisplayName("创建帖子时缺少标签")
        void testCreatePost_NoTags() {
            // Arrange
            createRequest.setTagIds(Arrays.asList());

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> postService.createPost(1L, createRequest));

            assertEquals("帖子必须包含至少一个标签", exception.getMessage());
            verify(tagMapper, never()).findById(anyLong());
            verify(postMapper, never()).insert(any(Post.class));
        }

        @Test
        @DisplayName("创建帖子时标签不存在")
        void testCreatePost_TagNotFound() {
            // Arrange
            when(tagMapper.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> postService.createPost(1L, createRequest));

            assertEquals("标签不存在或未启用: 1", exception.getMessage());
            verify(tagMapper).findById(1L);
            verify(tagMapper, never()).findById(2L);
            verify(postMapper, never()).insert(any(Post.class));
        }

        @Test
        @DisplayName("创建帖子时标签未启用")
        void testCreatePost_TagNotActive() {
            // Arrange
            Tag inactiveTag = new Tag();
            inactiveTag.setId(1L);
            inactiveTag.setIsActive(false);
            when(tagMapper.findById(1L)).thenReturn(Optional.of(inactiveTag));

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> postService.createPost(1L, createRequest));

            assertEquals("标签不存在或未启用: 1", exception.getMessage());
            verify(tagMapper).findById(1L);
            verify(tagMapper, never()).findById(2L);
            verify(postMapper, never()).insert(any(Post.class));
        }
    }

    @Nested
    @DisplayName("获取帖子详情测试")
    class GetPostTests {

        @Test
        @DisplayName("成功获取帖子详情")
        void testGetPostById_Success() {
            // Arrange
            when(postMapper.findById(1L)).thenReturn(Optional.of(testPost));
            when(postMapper.incrementViewCount(1L)).thenReturn(1);
            when(userMapper.findById(1L)).thenReturn(Optional.of(testUser));
            when(tagMapper.findTagsByPostId(1L)).thenReturn(Arrays.asList(testTag));
            when(postMapper.existsLike(1L, 1L)).thenReturn(true);

            // Act
            Post result = postService.getPostById(1L, 1L);

            // Assert
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("Test Post Title", result.getTitle());
            assertEquals(testUser, result.getAuthor());
            assertEquals(1, result.getTags().size());
            assertEquals("Java", result.getTags().get(0).getName());
            assertTrue(result.isLiked());

            verify(postMapper).findById(1L);
            verify(postMapper).incrementViewCount(1L);
            verify(userMapper).findById(1L);
            verify(tagMapper).findTagsByPostId(1L);
            verify(postMapper).existsLike(1L, 1L);
        }

        @Test
        @DisplayName("获取不存在的帖子")
        void testGetPostById_PostNotFound() {
            // Arrange
            when(postMapper.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> postService.getPostById(999L, 1L));

            assertEquals("帖子不存在: 999", exception.getMessage());
            verify(postMapper).findById(999L);
            verify(postMapper, never()).incrementViewCount(anyLong());
        }

        @Test
        @DisplayName("获取帖子详情时未登录")
        void testGetPostById_NotLoggedIn() {
            // Arrange
            when(postMapper.findById(1L)).thenReturn(Optional.of(testPost));
            when(postMapper.incrementViewCount(1L)).thenReturn(1);
            when(userMapper.findById(1L)).thenReturn(Optional.of(testUser));
            when(tagMapper.findTagsByPostId(1L)).thenReturn(Arrays.asList(testTag));

            // Act
            Post result = postService.getPostById(1L, null);

            // Assert
            assertNotNull(result);
            assertFalse(result.isLiked()); // 未登录用户不应显示已点赞

            verify(postMapper).findById(1L);
            verify(postMapper).incrementViewCount(1L);
            verify(userMapper).findById(1L);
            verify(tagMapper).findTagsByPostId(1L);
            verify(postMapper, never()).existsLike(anyLong(), anyLong());
        }
    }

    @Nested
    @DisplayName("更新帖子测试")
    class UpdatePostTests {

        @Test
        @DisplayName("成功更新帖子")
        void testUpdatePost_Success() {
            // Arrange
            Tag tag = new Tag();
            tag.setId(1L);
            tag.setIsActive(true);

            when(postMapper.findById(1L)).thenReturn(Optional.of(testPost));
            when(tagMapper.findById(1L)).thenReturn(Optional.of(tag));
            when(postMapper.update(any(Post.class))).thenReturn(1);
            when(tagMapper.deletePostTagsByPostId(1L)).thenReturn(2);
            when(tagMapper.insertPostTag(1L, 1L)).thenReturn(1);
            when(tagMapper.incrementUseCount(1L)).thenReturn(1);

            // 模拟getPostById的行为
            Post updatedPost = new Post();
            updatedPost.setId(1L);
            updatedPost.setUserId(1L);
            updatedPost.setTitle("Updated Title");
            updatedPost.setContent("Updated content");
            updatedPost.setSummary("Updated summary");
            updatedPost.setCoverImageUrl("updated-cover.jpg");
            updatedPost.setStatus(2);
            updatedPost.setVisibility(2);
            updatedPost.setIsTop(true);
            updatedPost.setAuthor(testUser);
            updatedPost.setTags(Arrays.asList(tag));
            updatedPost.setLiked(false);

            when(postMapper.findById(1L)).thenReturn(Optional.of(updatedPost));
            when(userMapper.findById(1L)).thenReturn(Optional.of(testUser));
            when(tagMapper.findTagsByPostId(1L)).thenReturn(Arrays.asList(tag));
            when(postMapper.existsLike(1L, 1L)).thenReturn(false);
            when(postMapper.incrementViewCount(1L)).thenReturn(1);

            // Act
            Post result = postService.updatePost(1L, 1L, updateRequest);

            // Assert
            assertNotNull(result);
            assertEquals("Updated Title", result.getTitle());
            assertEquals("Updated content", result.getContent());
            assertEquals("Updated summary", result.getSummary());
            assertEquals("updated-cover.jpg", result.getCoverImageUrl());
            assertEquals(2, result.getStatus());
            assertEquals(2, result.getVisibility());
            assertTrue(result.getIsTop());

            verify(postMapper, times(2)).findById(1L);
            verify(tagMapper).findById(1L);
            verify(postMapper).update(any(Post.class));
            verify(tagMapper).deletePostTagsByPostId(1L);
            verify(tagMapper).insertPostTag(1L, 1L);
            verify(tagMapper).incrementUseCount(1L);
        }

        @Test
        @DisplayName("更新不存在的帖子")
        void testUpdatePost_PostNotFound() {
            // Arrange
            when(postMapper.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> postService.updatePost(999L, 1L, updateRequest));

            assertEquals("帖子不存在: 999", exception.getMessage());
            verify(postMapper).findById(999L);
            verify(postMapper, never()).update(any(Post.class));
        }

        @Test
        @DisplayName("更新无权限的帖子")
        void testUpdatePost_NoPermission() {
            // Arrange
            when(postMapper.findById(1L)).thenReturn(Optional.of(testPost));

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> postService.updatePost(1L, 2L, updateRequest));

            assertEquals("没有权限更新此帖子", exception.getMessage());
            verify(postMapper).findById(1L);
            verify(postMapper, never()).update(any(Post.class));
        }

        @Test
        @DisplayName("部分更新帖子")
        void testUpdatePost_PartialUpdate() {
            // Arrange
            PostUpdateRequest partialRequest = new PostUpdateRequest();
            partialRequest.setTitle("Only Title Updated");

            when(postMapper.findById(1L)).thenReturn(Optional.of(testPost));
            when(postMapper.update(any(Post.class))).thenReturn(1);

            // 模拟getPostById的行为
            Post updatedPost = new Post();
            updatedPost.setId(1L);
            updatedPost.setUserId(1L);
            updatedPost.setTitle("Only Title Updated");
            updatedPost.setContent("Test post content"); // 保持不变
            updatedPost.setSummary("Test summary"); // 保持不变
            updatedPost.setCoverImageUrl("cover.jpg"); // 保持不变
            updatedPost.setStatus(1); // 保持不变
            updatedPost.setVisibility(1); // 保持不变
            updatedPost.setIsTop(false); // 保持不变
            updatedPost.setAuthor(testUser);
            updatedPost.setTags(Arrays.asList(testTag));
            updatedPost.setLiked(false);

            when(postMapper.findById(1L)).thenReturn(Optional.of(updatedPost));
            when(userMapper.findById(1L)).thenReturn(Optional.of(testUser));
            when(tagMapper.findTagsByPostId(1L)).thenReturn(Arrays.asList(testTag));
            when(postMapper.existsLike(1L, 1L)).thenReturn(false);
            when(postMapper.incrementViewCount(1L)).thenReturn(1);

            // Act
            Post result = postService.updatePost(1L, 1L, partialRequest);

            // Assert
            assertNotNull(result);
            assertEquals("Only Title Updated", result.getTitle());
            assertEquals("Test post content", result.getContent()); // 保持不变
            assertEquals("Test summary", result.getSummary()); // 保持不变

            verify(postMapper, times(2)).findById(1L);
            verify(postMapper).update(any(Post.class));
            verify(tagMapper, never()).deletePostTagsByPostId(anyLong());
            verify(tagMapper, never()).insertPostTag(anyLong(), anyLong());
        }
    }

    @Nested
    @DisplayName("删除帖子测试")
    class DeletePostTests {

        @Test
        @DisplayName("成功删除帖子")
        void testDeletePost_Success() {
            // Arrange
            when(postMapper.findById(1L)).thenReturn(Optional.of(testPost));
            when(postMapper.delete(1L)).thenReturn(1);
            when(userMapper.decrementPostCount(1L)).thenReturn(1);

            // Act
            postService.deletePost(1L, 1L);

            // Assert
            verify(postMapper).findById(1L);
            verify(postMapper).delete(1L);
            verify(userMapper).decrementPostCount(1L);
        }

        @Test
        @DisplayName("删除不存在的帖子")
        void testDeletePost_PostNotFound() {
            // Arrange
            when(postMapper.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> postService.deletePost(999L, 1L));

            assertEquals("帖子不存在: 999", exception.getMessage());
            verify(postMapper).findById(999L);
            verify(postMapper, never()).delete(anyLong());
        }

        @Test
        @DisplayName("删除无权限的帖子")
        void testDeletePost_NoPermission() {
            // Arrange
            when(postMapper.findById(1L)).thenReturn(Optional.of(testPost));

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> postService.deletePost(1L, 2L));

            assertEquals("没有权限删除此帖子", exception.getMessage());
            verify(postMapper).findById(1L);
            verify(postMapper, never()).delete(anyLong());
        }
    }

    @Nested
    @DisplayName("查询帖子测试")
    class QueryPostsTests {

        @Test
        @DisplayName("成功查询帖子列表")
        void testQueryPosts_Success() {
            // Arrange
            List<Post> posts = Arrays.asList(testPost);
            when(postMapper.findByCondition(queryRequest)).thenReturn(posts);
            when(userMapper.findById(1L)).thenReturn(Optional.of(testUser));
            when(tagMapper.findTagsByPostId(1L)).thenReturn(Arrays.asList(testTag));
            when(postMapper.existsLike(1L, 1L)).thenReturn(true);

            try (MockedStatic<PageHelper> pageHelperMock = mockStatic(PageHelper.class)) {
                // Act
                List<Post> result = postService.queryPosts(queryRequest);

                // Assert
                assertNotNull(result);
                assertEquals(1, result.size());
                assertEquals(1L, result.get(0).getId());
                assertEquals(testUser, result.get(0).getAuthor());
                assertEquals(1, result.get(0).getTags().size());
                assertTrue(result.get(0).isLiked());

                pageHelperMock.verify(() -> PageHelper.startPage(1, 10));
                verify(postMapper).findByCondition(queryRequest);
                verify(userMapper).findById(1L);
                verify(tagMapper).findTagsByPostId(1L);
                verify(postMapper).existsLike(1L, 1L);
            }
        }

        @Test
        @DisplayName("查询空帖子列表")
        void testQueryPosts_EmptyList() {
            // Arrange
            when(postMapper.findByCondition(queryRequest)).thenReturn(Arrays.asList());

            try (MockedStatic<PageHelper> pageHelperMock = mockStatic(PageHelper.class)) {
                // Act
                List<Post> result = postService.queryPosts(queryRequest);

                // Assert
                assertNotNull(result);
                assertTrue(result.isEmpty());

                pageHelperMock.verify(() -> PageHelper.startPage(1, 10));
                verify(postMapper).findByCondition(queryRequest);
                verify(userMapper, never()).findById(anyLong());
            }
        }

        @Test
        @DisplayName("统计帖子数量")
        void testCountPosts() {
            // Arrange
            when(postMapper.countByCondition(queryRequest)).thenReturn(100L);

            // Act
            long count = postService.countPosts(queryRequest);

            // Assert
            assertEquals(100L, count);
            verify(postMapper).countByCondition(queryRequest);
        }
    }

    @Nested
    @DisplayName("点赞功能测试")
    class LikeTests {

        @Test
        @DisplayName("成功点赞帖子")
        void testLikePost_Success() {
            // Arrange
            when(postMapper.existsLike(1L, 1L)).thenReturn(false);
            when(postMapper.insertLike(1L, 1L)).thenReturn(1);
            when(postMapper.incrementLikeCount(1L)).thenReturn(1);

            // Act
            postService.likePost(1L, 1L);

            // Assert
            verify(postMapper).existsLike(1L, 1L);
            verify(postMapper).insertLike(1L, 1L);
            verify(postMapper).incrementLikeCount(1L);
        }

        @Test
        @DisplayName("重复点赞")
        void testLikePost_AlreadyLiked() {
            // Arrange
            when(postMapper.existsLike(1L, 1L)).thenReturn(true);

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> postService.likePost(1L, 1L));

            assertEquals("已经点赞过此帖子", exception.getMessage());
            verify(postMapper).existsLike(1L, 1L);
            verify(postMapper, never()).insertLike(anyLong(), anyLong());
        }

        @Test
        @DisplayName("成功取消点赞")
        void testUnlikePost_Success() {
            // Arrange
            when(postMapper.existsLike(1L, 1L)).thenReturn(true);
            when(postMapper.deleteLike(1L, 1L)).thenReturn(1);
            when(postMapper.decrementLikeCount(1L)).thenReturn(1);

            // Act
            postService.unlikePost(1L, 1L);

            // Assert
            verify(postMapper).existsLike(1L, 1L);
            verify(postMapper).deleteLike(1L, 1L);
            verify(postMapper).decrementLikeCount(1L);
        }

        @Test
        @DisplayName("取消未点赞的帖子")
        void testUnlikePost_NotLiked() {
            // Arrange
            when(postMapper.existsLike(1L, 1L)).thenReturn(false);

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> postService.unlikePost(1L, 1L));

            assertEquals("尚未点赞此帖子", exception.getMessage());
            verify(postMapper).existsLike(1L, 1L);
            verify(postMapper, never()).deleteLike(anyLong(), anyLong());
        }

        @Test
        @DisplayName("检查点赞状态")
        void testIsLiked() {
            // Arrange
            when(postMapper.existsLike(1L, 1L)).thenReturn(true);
            when(postMapper.existsLike(2L, 1L)).thenReturn(false);

            // Act & Assert
            assertTrue(postService.isLiked(1L, 1L));
            assertFalse(postService.isLiked(1L, 2L));

            verify(postMapper).existsLike(1L, 1L);
            verify(postMapper).existsLike(1L, 2L);
        }
    }

    @Nested
    @DisplayName("其他查询功能测试")
    class OtherQueryTests {

        @Test
        @DisplayName("获取用户帖子")
        void testGetUserPosts() {
            // Arrange
            List<Post> posts = Arrays.asList(testPost);
            when(postMapper.findByUserId(1L, 1)).thenReturn(posts);
            when(userMapper.findById(1L)).thenReturn(Optional.of(testUser));
            when(postMapper.existsLike(1L, 1L)).thenReturn(true);

            // Act
            List<Post> result = postService.getUserPosts(1L, 1);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(1L, result.get(0).getId());
            assertNotNull(result.get(0).getAuthor());
            assertTrue(result.get(0).isLiked());

            verify(postMapper).findByUserId(1L, 1);
            verify(userMapper).findById(1L);
            verify(postMapper).existsLike(1L, 1L);
        }

        @Test
        @DisplayName("获取关注用户的帖子")
        void testGetFollowingPosts() {
            // Arrange
            List<Post> posts = Arrays.asList(testPost);
            when(postMapper.findFollowingPosts(1L)).thenReturn(posts);
            when(userMapper.findById(1L)).thenReturn(Optional.of(testUser));
            when(postMapper.existsLike(1L, 1L)).thenReturn(true);

            // Act
            List<Post> result = postService.getFollowingPosts(1L);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(1L, result.get(0).getId());
            assertNotNull(result.get(0).getAuthor());
            assertTrue(result.get(0).isLiked());

            verify(postMapper).findFollowingPosts(1L);
            verify(userMapper).findById(1L);
            verify(postMapper).existsLike(1L, 1L);
        }

        @Test
        @DisplayName("根据标签获取帖子")
        void testGetPostsByTagId() {
            // Arrange
            List<Post> posts = Arrays.asList(testPost);
            when(postMapper.findByTagId(1L)).thenReturn(posts);
            when(userMapper.findById(1L)).thenReturn(Optional.of(testUser));
            when(postMapper.existsLike(anyLong(), eq(1L))).thenReturn(false);

            // Act
            List<Post> result = postService.getPostsByTagId(1L);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(1L, result.get(0).getId());
            assertNotNull(result.get(0).getAuthor());
            assertFalse(result.get(0).isLiked());

            verify(postMapper).findByTagId(1L);
            verify(userMapper).findById(1L);
            verify(postMapper).existsLike(anyLong(), eq(1L));
        }
    }
}