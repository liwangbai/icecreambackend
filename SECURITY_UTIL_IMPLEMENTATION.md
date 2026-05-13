# SecurityUtil 工具类实现说明

## 问题背景

在Spring Boot应用中，所有Controller（UserController、PostController等）都有一个私有的`getCurrentUserId()`方法，该方法返回固定的示例ID（1L）。这个方法被标记为TODO，注释说明应该从Spring Security的SecurityContext中获取实际用户ID。

## 解决方案

创建了`SecurityUtil`工具类，提供从Spring Security SecurityContext获取当前用户信息的静态方法，并更新所有Controller使用这个工具类。

## 实现细节

### 1. SecurityUtil 工具类

**路径**: `src/main/java/com/icecream/backend/util/SecurityUtil.java`

**主要功能**:
- `getCurrentUserId()`: 从SecurityContext获取当前用户的Long类型ID
- `getCurrentUsername()`: 获取当前用户名
- `getCurrentUser()`: 获取User实体对象
- `isCurrentUser(Long userId)`: 检查给定ID是否是当前用户
- `hasRole(String role)`: 检查当前用户是否有指定角色
- `isAdmin()`: 检查当前用户是否是管理员
- `hasAccess(Long resourceOwnerId)`: 验证当前用户是否具有访问权限（管理员或资源所有者）

**技术实现**:
- 使用`SecurityContextHolder.getContext().getAuthentication()`获取当前认证信息
- 从`Authentication`中获取`UserDetails`对象
- 根据用户名查询数据库获取用户ID
- 使用`@PostConstruct`确保静态字段正确注入

### 2. Controller 更新

**UserController** (`src/main/java/com/icecream/backend/controller/UserController.java`):
- 删除了私有的`getCurrentUserId()`方法
- 在所有需要当前用户ID的地方使用`SecurityUtil.getCurrentUserId()`
- 更新了以下方法:
  - `getCurrentUser()`: 获取当前用户信息
  - `updateCurrentUser()`: 更新当前用户信息
  - `followUser()`: 关注用户
  - `unfollowUser()`: 取消关注
  - `isFollowing()`: 检查是否关注

**PostController** (`src/main/java/com/icecream/backend/controller/PostController.java`):
- 删除了私有的`getCurrentUserId()`方法
- 在所有需要当前用户ID的地方使用`SecurityUtil.getCurrentUserId()`
- 更新了以下方法:
  - `createPost()`: 创建帖子
  - `getPostById()`: 获取帖子详情
  - `updatePost()`: 更新帖子
  - `deletePost()`: 删除帖子
  - `queryPosts()`: 查询帖子列表
  - `likePost()`: 点赞帖子
  - `unlikePost()`: 取消点赞
  - `isLiked()`: 检查是否点赞
  - `getFollowingPosts()`: 获取关注用户帖子

### 3. 其他Controller

**AuthController**: 不需要更新，处理认证相关操作（注册、登录、刷新令牌、登出）
**TagController**: 不需要更新，处理标签管理，大部分操作需要管理员权限

## 技术要点

### 1. JWT认证流程
1. `JwtAuthenticationFilter`验证JWT令牌
2. 从令牌中提取用户名
3. `CustomUserDetailsService`从数据库加载用户信息
4. 创建`Authentication`对象并设置到`SecurityContext`
5. `SecurityUtil`从`SecurityContext`获取当前用户信息

### 2. 用户ID获取逻辑
1. 从`SecurityContext`获取当前认证信息
2. 从`Authentication`中获取用户名
3. 使用`UserMapper`根据用户名查询数据库获取用户ID
4. 返回用户ID或抛出异常

### 3. 异常处理
- `IllegalStateException`: 用户未认证或用户不存在
- 在`SecurityUtil`中提供友好的错误信息和日志记录

## 使用示例

### 获取当前用户ID
```java
Long currentUserId = SecurityUtil.getCurrentUserId();
```

### 检查当前用户是否是管理员
```java
if (SecurityUtil.isAdmin()) {
    // 管理员操作
}
```

### 验证访问权限
```java
// 检查用户是否是资源所有者或管理员
if (SecurityUtil.hasAccess(resourceOwnerId)) {
    // 允许访问
}

// 或者直接验证，如果不具备权限会抛出异常
SecurityUtil.validateAccess(resourceOwnerId);
```

### 获取当前用户信息
```java
User currentUser = SecurityUtil.getCurrentUser();
String username = SecurityUtil.getCurrentUsername();
String role = SecurityUtil.getCurrentUserRole();
```

## 注意事项

1. **性能考虑**: `SecurityUtil.getCurrentUserId()`每次调用都会查询数据库，可以考虑缓存用户ID以减少数据库查询
2. **线程安全**: `SecurityUtil`使用静态方法，但通过`SecurityContextHolder`确保线程安全
3. **异常处理**: 在Controller中适当处理`IllegalStateException`，返回合适的HTTP状态码
4. **测试**: 需要编写单元测试和集成测试验证`SecurityUtil`的正确性

## 文件列表

1. `src/main/java/com/icecream/backend/util/SecurityUtil.java` - 安全工具类
2. `src/main/java/com/icecream/backend/controller/UserController.java` - 更新后的用户控制器
3. `src/main/java/com/icecream/backend/controller/PostController.java` - 更新后的帖子控制器

## 后续改进建议

1. **缓存优化**: 实现用户ID缓存，减少数据库查询
2. **自定义UserDetails**: 创建包含用户ID的自定义`UserDetails`实现，避免数据库查询
3. **测试覆盖**: 添加完整的单元测试和集成测试
4. **监控日志**: 添加更详细的日志记录和监控
5. **性能优化**: 考虑使用Redis缓存用户信息