package com.icecream.backend.service.impl;

import com.icecream.backend.dto.request.TagCreateRequest;
import com.icecream.backend.dto.request.TagUpdateRequest;
import com.icecream.backend.mapper.TagMapper;
import com.icecream.backend.mapper.UserMapper;
import com.icecream.backend.model.Tag;
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
 * 标签服务单元测试类
 * 测试TagServiceImpl的各种标签管理场景
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("标签服务测试")
class TagServiceImplTest {

    @Mock
    private TagMapper tagMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private TagServiceImpl tagService;

    private Tag testTag;
    private Tag anotherTag;
    private TagCreateRequest createRequest;
    private TagUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        // 初始化测试标签
        testTag = new Tag();
        testTag.setId(1L);
        testTag.setName("Java");
        testTag.setDescription("Java programming language");
        testTag.setColor("#FF0000");
        testTag.setIcon("java-icon.png");
        testTag.setIsActive(true);
        testTag.setUseCount(100);
        testTag.setSortOrder(1);
        testTag.setCreatedBy(1L);

        // 初始化另一个标签
        anotherTag = new Tag();
        anotherTag.setId(2L);
        anotherTag.setName("Spring");
        anotherTag.setDescription("Spring Framework");
        anotherTag.setColor("#00FF00");
        anotherTag.setIcon("spring-icon.png");
        anotherTag.setIsActive(true);
        anotherTag.setUseCount(50);
        anotherTag.setSortOrder(2);
        anotherTag.setCreatedBy(1L);

        // 初始化创建请求
        createRequest = new TagCreateRequest();
        createRequest.setName("New Tag");
        createRequest.setDescription("New tag description");
        createRequest.setColor("#0000FF");
        createRequest.setIcon("new-icon.png");
        createRequest.setIsActive(true);
        createRequest.setSortOrder(3);

        // 初始化更新请求
        updateRequest = new TagUpdateRequest();
        updateRequest.setName("Updated Tag");
        updateRequest.setDescription("Updated description");
        updateRequest.setColor("#FFFF00");
        updateRequest.setIcon("updated-icon.png");
        updateRequest.setIsActive(false);
        updateRequest.setSortOrder(5);
    }

    @Nested
    @DisplayName("获取标签测试")
    class GetTagTests {

        @Test
        @DisplayName("成功获取所有标签")
        void testGetAllTags_Success() {
            // Arrange
            List<Tag> tags = Arrays.asList(testTag, anotherTag);
            when(tagMapper.findAll()).thenReturn(tags);

            // Act
            List<Tag> result = tagService.getAllTags();

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("Java", result.get(0).getName());
            assertEquals("Spring", result.get(1).getName());

            verify(tagMapper).findAll();
        }

        @Test
        @DisplayName("获取空标签列表")
        void testGetAllTags_EmptyList() {
            // Arrange
            when(tagMapper.findAll()).thenReturn(Arrays.asList());

            // Act
            List<Tag> result = tagService.getAllTags();

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(tagMapper).findAll();
        }

        @Test
        @DisplayName("成功获取启用标签")
        void testGetActiveTags_Success() {
            // Arrange
            List<Tag> activeTags = Arrays.asList(testTag, anotherTag);
            when(tagMapper.findActiveTags()).thenReturn(activeTags);

            // Act
            List<Tag> result = tagService.getActiveTags();

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.get(0).getIsActive());
            assertTrue(result.get(1).getIsActive());

            verify(tagMapper).findActiveTags();
        }

        @Test
        @DisplayName("成功获取标签详情")
        void testGetTagById_Success() {
            // Arrange
            when(tagMapper.findById(1L)).thenReturn(Optional.of(testTag));

            // Act
            Tag result = tagService.getTagById(1L);

            // Assert
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("Java", result.getName());
            assertEquals("Java programming language", result.getDescription());
            assertEquals("#FF0000", result.getColor());

            verify(tagMapper).findById(1L);
        }

        @Test
        @DisplayName("获取不存在的标签")
        void testGetTagById_TagNotFound() {
            // Arrange
            when(tagMapper.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> tagService.getTagById(999L));

            assertEquals("标签不存在: 999", exception.getMessage());
            verify(tagMapper).findById(999L);
        }

        @Test
        @DisplayName("根据名称获取标签")
        void testGetTagByName_Success() {
            // Arrange
            when(tagMapper.findByName("Java")).thenReturn(Optional.of(testTag));

            // Act
            Tag result = tagService.getTagByName("Java");

            // Assert
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("Java", result.getName());

            verify(tagMapper).findByName("Java");
        }

        @Test
        @DisplayName("根据名称获取不存在的标签")
        void testGetTagByName_TagNotFound() {
            // Arrange
            when(tagMapper.findByName("Nonexistent")).thenReturn(Optional.empty());

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> tagService.getTagByName("Nonexistent"));

            assertEquals("标签不存在: Nonexistent", exception.getMessage());
            verify(tagMapper).findByName("Nonexistent");
        }
    }

    @Nested
    @DisplayName("创建标签测试")
    class CreateTagTests {

        @Test
        @DisplayName("成功创建标签")
        void testCreateTag_Success() {
            // Arrange
            when(tagMapper.findByName("New Tag")).thenReturn(Optional.empty());
            when(tagMapper.insert(any(Tag.class))).thenAnswer(invocation -> {
                Tag tag = invocation.getArgument(0);
                tag.setId(3L); // 模拟数据库生成ID
                return 1;
            });

            // Act
            Tag result = tagService.createTag(createRequest);

            // Assert
            assertNotNull(result);
            assertEquals(3L, result.getId());
            assertEquals("New Tag", result.getName());
            assertEquals("New tag description", result.getDescription());
            assertEquals("#0000FF", result.getColor());
            assertEquals("new-icon.png", result.getIcon());
            assertTrue(result.getIsActive());
            assertEquals(3, result.getSortOrder());
            assertEquals(0, result.getUseCount()); // 新标签使用次数为0
            assertEquals(1L, result.getCreatedBy()); // TODO: 应从SecurityContext获取

            verify(tagMapper).findByName("New Tag");
            verify(tagMapper).insert(any(Tag.class));
        }

        @Test
        @DisplayName("创建标签时名称已存在")
        void testCreateTag_NameAlreadyExists() {
            // Arrange
            when(tagMapper.findByName("New Tag")).thenReturn(Optional.of(testTag));

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> tagService.createTag(createRequest));

            assertEquals("标签名称已存在: New Tag", exception.getMessage());
            verify(tagMapper).findByName("New Tag");
            verify(tagMapper, never()).insert(any(Tag.class));
        }
    }

    @Nested
    @DisplayName("更新标签测试")
    class UpdateTagTests {

        @Test
        @DisplayName("成功更新标签")
        void testUpdateTag_Success() {
            // Arrange
            when(tagMapper.findById(1L)).thenReturn(Optional.of(testTag));
            when(tagMapper.findByName("Updated Tag")).thenReturn(Optional.empty());
            when(tagMapper.update(any(Tag.class))).thenReturn(1);

            // 模拟getTagById的行为
            Tag updatedTag = new Tag();
            updatedTag.setId(1L);
            updatedTag.setName("Updated Tag");
            updatedTag.setDescription("Updated description");
            updatedTag.setColor("#FFFF00");
            updatedTag.setIcon("updated-icon.png");
            updatedTag.setIsActive(false);
            updatedTag.setUseCount(100);
            updatedTag.setSortOrder(5);
            updatedTag.setCreatedBy(1L);

            when(tagMapper.findById(1L)).thenReturn(Optional.of(updatedTag));

            // Act
            Tag result = tagService.updateTag(1L, updateRequest);

            // Assert
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("Updated Tag", result.getName());
            assertEquals("Updated description", result.getDescription());
            assertEquals("#FFFF00", result.getColor());
            assertEquals("updated-icon.png", result.getIcon());
            assertFalse(result.getIsActive());
            assertEquals(5, result.getSortOrder());

            verify(tagMapper, times(2)).findById(1L);
            verify(tagMapper).findByName("Updated Tag");
            verify(tagMapper).update(any(Tag.class));
        }

        @Test
        @DisplayName("更新不存在的标签")
        void testUpdateTag_TagNotFound() {
            // Arrange
            when(tagMapper.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> tagService.updateTag(999L, updateRequest));

            assertEquals("标签不存在: 999", exception.getMessage());
            verify(tagMapper).findById(999L);
            verify(tagMapper, never()).findByName(anyString());
            verify(tagMapper, never()).update(any(Tag.class));
        }

        @Test
        @DisplayName("更新标签时新名称已被其他标签使用")
        void testUpdateTag_NameAlreadyUsed() {
            // Arrange
            when(tagMapper.findById(1L)).thenReturn(Optional.of(testTag));
            when(tagMapper.findByName("Updated Tag")).thenReturn(Optional.of(anotherTag));

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> tagService.updateTag(1L, updateRequest));

            assertEquals("标签名称已被其他标签使用: Updated Tag", exception.getMessage());
            verify(tagMapper).findById(1L);
            verify(tagMapper).findByName("Updated Tag");
            verify(tagMapper, never()).update(any(Tag.class));
        }

        @Test
        @DisplayName("更新标签时名称未改变")
        void testUpdateTag_NameNotChanged() {
            // Arrange
            updateRequest.setName("Java"); // 与原名称相同
            when(tagMapper.findById(1L)).thenReturn(Optional.of(testTag));
            when(tagMapper.update(any(Tag.class))).thenReturn(1);

            // 模拟getTagById的行为
            Tag updatedTag = new Tag();
            updatedTag.setId(1L);
            updatedTag.setName("Java");
            updatedTag.setDescription("Updated description");
            updatedTag.setColor("#FFFF00");
            updatedTag.setIcon("updated-icon.png");
            updatedTag.setIsActive(false);
            updatedTag.setUseCount(100);
            updatedTag.setSortOrder(5);
            updatedTag.setCreatedBy(1L);

            when(tagMapper.findById(1L)).thenReturn(Optional.of(updatedTag));

            // Act
            Tag result = tagService.updateTag(1L, updateRequest);

            // Assert
            assertNotNull(result);
            assertEquals("Java", result.getName()); // 名称保持不变

            verify(tagMapper, times(2)).findById(1L);
            verify(tagMapper, never()).findByName(anyString()); // 不应该检查名称是否被使用
            verify(tagMapper).update(any(Tag.class));
        }

        @Test
        @DisplayName("部分更新标签")
        void testUpdateTag_PartialUpdate() {
            // Arrange
            TagUpdateRequest partialRequest = new TagUpdateRequest();
            partialRequest.setDescription("Only description updated");

            when(tagMapper.findById(1L)).thenReturn(Optional.of(testTag));
            when(tagMapper.update(any(Tag.class))).thenReturn(1);

            // 模拟getTagById的行为
            Tag updatedTag = new Tag();
            updatedTag.setId(1L);
            updatedTag.setName("Java"); // 保持不变
            updatedTag.setDescription("Only description updated");
            updatedTag.setColor("#FF0000"); // 保持不变
            updatedTag.setIcon("java-icon.png"); // 保持不变
            updatedTag.setIsActive(true); // 保持不变
            updatedTag.setUseCount(100);
            updatedTag.setSortOrder(1); // 保持不变
            updatedTag.setCreatedBy(1L);

            when(tagMapper.findById(1L)).thenReturn(Optional.of(updatedTag));

            // Act
            Tag result = tagService.updateTag(1L, partialRequest);

            // Assert
            assertNotNull(result);
            assertEquals("Java", result.getName()); // 保持不变
            assertEquals("Only description updated", result.getDescription());
            assertEquals("#FF0000", result.getColor()); // 保持不变
            assertEquals("java-icon.png", result.getIcon()); // 保持不变
            assertTrue(result.getIsActive()); // 保持不变

            verify(tagMapper, times(2)).findById(1L);
            verify(tagMapper, never()).findByName(anyString());
            verify(tagMapper).update(any(Tag.class));
        }
    }

    @Nested
    @DisplayName("删除标签测试")
    class DeleteTagTests {

        @Test
        @DisplayName("成功删除标签")
        void testDeleteTag_Success() {
            // Arrange
            when(tagMapper.findById(1L)).thenReturn(Optional.of(testTag));
            when(tagMapper.delete(1L)).thenReturn(1);

            // Act
            tagService.deleteTag(1L);

            // Assert
            verify(tagMapper).findById(1L);
            verify(tagMapper).delete(1L);
        }

        @Test
        @DisplayName("删除不存在的标签")
        void testDeleteTag_TagNotFound() {
            // Arrange
            when(tagMapper.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> tagService.deleteTag(999L));

            assertEquals("标签不存在: 999", exception.getMessage());
            verify(tagMapper).findById(999L);
            verify(tagMapper, never()).delete(anyLong());
        }

        @Test
        @DisplayName("删除正在被使用的标签")
        void testDeleteTag_TagInUse() {
            // Arrange
            testTag.setUseCount(10); // 标签正在被使用
            when(tagMapper.findById(1L)).thenReturn(Optional.of(testTag));

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> tagService.deleteTag(1L));

            assertEquals("标签正在被使用，无法删除", exception.getMessage());
            verify(tagMapper).findById(1L);
            verify(tagMapper, never()).delete(anyLong());
        }
    }

    @Nested
    @DisplayName("其他功能测试")
    class OtherTests {

        @Test
        @DisplayName("获取热门标签")
        void testGetPopularTags() {
            // Arrange
            List<Tag> popularTags = Arrays.asList(testTag, anotherTag);
            when(tagMapper.findPopularTags(10)).thenReturn(popularTags);

            // Act
            List<Tag> result = tagService.getPopularTags(10);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("Java", result.get(0).getName());
            assertEquals("Spring", result.get(1).getName());

            verify(tagMapper).findPopularTags(10);
        }

        @Test
        @DisplayName("根据帖子ID获取标签")
        void testGetTagsByPostId() {
            // Arrange
            List<Tag> tags = Arrays.asList(testTag, anotherTag);
            when(tagMapper.findTagsByPostId(1L)).thenReturn(tags);

            // Act
            List<Tag> result = tagService.getTagsByPostId(1L);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("Java", result.get(0).getName());
            assertEquals("Spring", result.get(1).getName());

            verify(tagMapper).findTagsByPostId(1L);
        }

        @Test
        @DisplayName("增加标签使用次数")
        void testIncrementUseCount() {
            // Arrange
            when(tagMapper.incrementUseCount(1L)).thenReturn(1);

            // Act
            tagService.incrementUseCount(1L);

            // Assert
            verify(tagMapper).incrementUseCount(1L);
        }

        @Test
        @DisplayName("减少标签使用次数")
        void testDecrementUseCount() {
            // Arrange
            when(tagMapper.decrementUseCount(1L)).thenReturn(1);

            // Act
            tagService.decrementUseCount(1L);

            // Assert
            verify(tagMapper).decrementUseCount(1L);
        }
    }
}