# 文件上传功能使用说明

## 概述

本文档介绍了Spring Boot应用中的文件上传功能的实现和使用方法。该功能支持单个文件上传、批量上传、文件类型验证、大小限制、安全防护等特性。

## 功能特性

1. **基本文件上传**：支持单个文件上传
2. **批量上传**：支持最多5个文件同时上传
3. **文件类型验证**：支持图片格式（JPG、PNG、GIF、WebP、BMP、SVG）
4. **文件大小限制**：默认10MB，可配置
5. **安全防护**：
   - 防止路径遍历攻击
   - 文件类型白名单验证
   - 用户权限验证
6. **目录组织**：
   - 按文件类型分目录（avatars、posts、others）
   - 按用户分目录（可选）
   - 按日期分层存储
7. **唯一文件名**：自动生成UUID文件名，防止冲突
8. **REST API**：完整的RESTful API接口

## 配置说明

### 配置文件（application.yml）

```yaml
file:
  upload:
    # 文件上传基础目录
    base-dir: ./uploads
    # 最大文件大小（字节），默认10MB
    max-file-size: 10485760
    # 允许的文件类型（MIME类型）
    allowed-file-types:
      - image/jpeg
      - image/jpg
      - image/png
      - image/gif
      - image/webp
      - image/bmp
      - image/svg+xml
    # 允许的文件扩展名
    allowed-extensions:
      - jpg
      - jpeg
      - png
      - gif
      - webp
      - bmp
      - svg
    # 是否启用文件类型验证
    enable-file-type-validation: true
    # 是否启用文件大小验证
    enable-file-size-validation: true
    # 是否生成唯一文件名
    generate-unique-filename: true
    # 文件访问URL前缀
    url-prefix: /uploads
    # 是否按用户分目录存储
    organize-by-user: true
    # 是否按文件类型分目录存储
    organize-by-type: true
    # 用户头像存储目录名
    avatar-dir: avatars
    # 帖子图片存储目录名
    post-dir: posts
    # 其他文件存储目录名
    other-dir: others
    # 是否启用文件MD5校验
    enable-md5-check: false
    # 是否覆盖同名文件
    overwrite-existing: false
    # 文件存储模式（local:本地存储）
    storage-mode: local
```

### 环境变量配置

可以通过环境变量覆盖配置文件中的设置：

```bash
# 文件上传目录
export FILE_UPLOAD_BASE_DIR=/data/uploads

# 最大文件大小（字节）
export FILE_UPLOAD_MAX_FILE_SIZE=20971520  # 20MB

# 文件访问URL前缀
export FILE_UPLOAD_URL_PREFIX=/static/uploads
```

## API接口

### 1. 上传单个文件

**Endpoint**: `POST /api/v1/upload`

**Headers**:
- `Content-Type: multipart/form-data`
- `Authorization: Bearer {token}`

**Parameters**:
- `file` (required): 上传的文件
- `category` (required): 文件分类（avatars、posts、others）
- `description` (optional): 文件描述

**Example Request**:
```bash
curl -X POST "http://localhost:8080/api/v1/upload" \
  -H "Authorization: Bearer {token}" \
  -F "file=@avatar.jpg" \
  -F "category=avatars" \
  -F "description=用户头像"
```

**Response**:
```json
{
  "success": true,
  "message": "文件上传成功",
  "data": {
    "originalFilename": "avatar.jpg",
    "storedFilename": "a1b2c3d4e5f6.jpg",
    "fileUrl": "/uploads/avatars/1/a1b2c3d4e5f6.jpg",
    "fileSize": 102400,
    "fileType": "image/jpeg",
    "storagePath": "./uploads/avatars/2024/01/15/1/a1b2c3d4e5f6.jpg",
    "uploadTime": "2024-01-15T10:30:00",
    "category": "avatars",
    "description": "用户头像",
    "md5": "d41d8cd98f00b204e9800998ecf8427e"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

### 2. 上传用户头像（专用接口）

**Endpoint**: `POST /api/v1/upload/avatar`

**Headers**:
- `Content-Type: multipart/form-data`
- `Authorization: Bearer {token}`

**Parameters**:
- `file` (required): 头像文件

**Example Request**:
```bash
curl -X POST "http://localhost:8080/api/v1/upload/avatar" \
  -H "Authorization: Bearer {token}" \
  -F "file=@avatar.jpg"
```

### 3. 上传帖子图片（专用接口）

**Endpoint**: `POST /api/v1/upload/post`

**Headers**:
- `Content-Type: multipart/form-data`
- `Authorization: Bearer {token}`

**Parameters**:
- `file` (required): 帖子图片文件
- `description` (optional): 图片描述

**Example Request**:
```bash
curl -X POST "http://localhost:8080/api/v1/upload/post" \
  -H "Authorization: Bearer {token}" \
  -F "file=@post-image.jpg" \
  -F "description=帖子配图"
```

### 4. 批量上传文件

**Endpoint**: `POST /api/v1/upload/batch`

**Headers**:
- `Content-Type: multipart/form-data`
- `Authorization: Bearer {token}`

**Parameters**:
- `files` (required): 文件数组（最多5个）
- `category` (required): 文件分类

**Example Request**:
```bash
curl -X POST "http://localhost:8080/api/v1/upload/batch" \
  -H "Authorization: Bearer {token}" \
  -F "files=@image1.jpg" \
  -F "files=@image2.jpg" \
  -F "category=posts"
```

### 5. 删除文件

**Endpoint**: `DELETE /api/v1/upload/{category}/{filename}`

**Headers**:
- `Authorization: Bearer {token}`

**Example Request**:
```bash
curl -X DELETE "http://localhost:8080/api/v1/upload/avatars/a1b2c3d4e5f6.jpg" \
  -H "Authorization: Bearer {token}"
```

**Response**:
```json
{
  "success": true,
  "message": "文件删除成功",
  "data": null,
  "timestamp": "2024-01-15T10:35:00"
}
```

### 6. 获取文件信息

**Endpoint**: `GET /api/v1/upload/{category}/{filename}/info`

**Example Request**:
```bash
curl -X GET "http://localhost:8080/api/v1/upload/avatars/a1b2c3d4e5f6.jpg/info"
```

**Response**:
```json
{
  "success": true,
  "message": null,
  "data": {
    "filename": "a1b2c3d4e5f6.jpg",
    "fileUrl": "/uploads/avatars/1/a1b2c3d4e5f6.jpg",
    "fileSize": 102400,
    "fileType": "image/jpeg",
    "lastModified": "2024-01-15T10:30:00",
    "createdTime": "2024-01-15T10:30:00",
    "readable": true,
    "writable": true,
    "executable": false,
    "category": "avatars",
    "ownerId": 1,
    "description": "用户头像"
  },
  "timestamp": "2024-01-15T10:40:00"
}
```

### 7. 下载文件

**Endpoint**: `GET /api/v1/upload/{category}/{filename}`

**Example Request**:
```bash
curl -X GET "http://localhost:8080/api/v1/upload/avatars/a1b2c3d4e5f6.jpg" \
  -o downloaded.jpg
```

### 8. 检查文件是否存在

**Endpoint**: `GET /api/v1/upload/{category}/{filename}/exists`

**Example Request**:
```bash
curl -X GET "http://localhost:8080/api/v1/upload/avatars/a1b2c3d4e5f6.jpg/exists"
```

**Response**:
```json
{
  "success": true,
  "message": null,
  "data": true,
  "timestamp": "2024-01-15T10:45:00"
}
```

## 文件存储结构

上传的文件按照以下目录结构存储：

```
uploads/
├── avatars/                    # 用户头像目录
│   ├── 2024/                  # 年份
│   │   ├── 01/               # 月份
│   │   │   ├── 15/          # 日期
│   │   │   │   ├── 1/       # 用户ID目录（如果启用）
│   │   │   │   │   └── a1b2c3d4e5f6.jpg
│   │   │   │   └── 2/
│   │   │   │       └── b2c3d4e5f6g7h8.jpg
│   │   │   └── 16/
│   │   └── 02/
│   └── 2023/
├── posts/                     # 帖子图片目录
│   ├── 2024/
│   │   ├── 01/
│   │   │   ├── 15/
│   │   │   │   ├── 1/
│   │   │   │   │   └── c3d4e5f6g7h8i9.jpg
│   │   │   │   └── 3/
│   │   │   │       └── d4e5f6g7h8i9j0.jpg
│   │   │   └── 16/
│   │   └── 02/
│   └── 2023/
└── others/                    # 其他文件目录
    ├── 2024/
    │   ├── 01/
    │   │   ├── 15/
    │   │   │   └── 1/
    │   │   │       └── e5f6g7h8i9j0k1.pdf
    │   │   └── 16/
    │   └── 02/
    └── 2023/
```

## 安全特性

### 1. 文件类型验证
- 只允许白名单中的文件类型上传
- 同时验证MIME类型和文件扩展名
- 默认支持常见的图片格式

### 2. 文件大小限制
- 默认最大10MB
- 可配置大小限制
- 上传前验证文件大小

### 3. 路径遍历防护
- 检测文件名中的`..`、`/`、`\`等字符
- 防止访问系统目录
- 自动清理非法字符

### 4. 权限控制
- 需要认证才能上传文件
- 用户只能删除自己的文件
- 管理员可以删除所有文件

### 5. 唯一文件名
- 自动生成UUID文件名
- 防止文件名冲突
- 防止恶意文件覆盖

## 错误处理

### 常见错误响应

1. **文件大小超过限制**
```json
{
  "success": false,
  "message": "文件大小超过限制，最大允许10485760字节",
  "data": null,
  "timestamp": "2024-01-15T10:50:00"
}
```

2. **文件类型不支持**
```json
{
  "success": false,
  "message": "文件类型不支持，仅支持：image/jpeg, image/jpg, image/png, image/gif, image/webp, image/bmp, image/svg+xml",
  "data": null,
  "timestamp": "2024-01-15T10:51:00"
}
```

3. **文件为空**
```json
{
  "success": false,
  "message": "上传的文件为空",
  "data": null,
  "timestamp": "2024-01-15T10:52:00"
}
```

4. **未认证**
```json
{
  "success": false,
  "message": "访问被拒绝：需要认证",
  "data": null,
  "timestamp": "2024-01-15T10:53:00"
}
```

## 集成示例

### 1. 前端上传示例（JavaScript）

```javascript
async function uploadAvatar(file) {
    const formData = new FormData();
    formData.append('file', file);
    
    const response = await fetch('/api/v1/upload/avatar', {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: formData
    });
    
    const result = await response.json();
    
    if (result.success) {
        console.log('上传成功:', result.data.fileUrl);
        return result.data;
    } else {
        console.error('上传失败:', result.message);
        throw new Error(result.message);
    }
}

// 使用示例
const fileInput = document.getElementById('avatar-input');
fileInput.addEventListener('change', async (event) => {
    const file = event.target.files[0];
    if (file) {
        try {
            const result = await uploadAvatar(file);
            // 更新用户头像显示
            document.getElementById('avatar-img').src = result.fileUrl;
        } catch (error) {
            alert('头像上传失败: ' + error.message);
        }
    }
});
```

### 2. 后端服务调用示例

```java
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final FileUploadService fileUploadService;
    
    public User updateUserAvatar(Long userId, MultipartFile avatarFile) throws IOException {
        // 上传头像文件
        FileUploadResponse response = fileUploadService.uploadFile(
            avatarFile, "avatars", userId, "用户头像");
        
        // 更新用户头像URL
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        
        user.setAvatarUrl(response.getFileUrl());
        userRepository.save(user);
        
        return user;
    }
    
    public void deleteUserAvatar(Long userId) {
        // 这里需要根据业务逻辑获取用户的头像文件名
        String avatarFilename = getAvatarFilename(userId);
        
        // 删除头像文件
        boolean deleted = fileUploadService.deleteFile(
            avatarFilename, "avatars", userId);
        
        if (deleted) {
            // 更新用户头像URL为空
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
            
            user.setAvatarUrl(null);
            userRepository.save(user);
        }
    }
}
```

## 扩展性

### 1. 支持云存储

文件上传服务设计为可扩展的，未来可以轻松替换为云存储（如阿里云OSS、AWS S3等）：

1. 创建新的服务实现类（如`OssFileUploadServiceImpl`）
2. 实现`FileUploadService`接口
3. 在配置中设置`storage-mode: oss`
4. 添加云存储相关的配置属性

### 2. 添加新的文件类型

要添加新的文件类型支持：

1. 在`application.yml`中添加MIME类型：
```yaml
allowed-file-types:
  - image/jpeg
  - image/jpg
  - image/png
  # 添加新的类型
  - application/pdf
  - text/plain
```

2. 添加文件扩展名：
```yaml
allowed-extensions:
  - jpg
  - jpeg
  - png
  # 添加新的扩展名
  - pdf
  - txt
```

### 3. 自定义存储目录

可以通过配置自定义存储目录结构：

```yaml
file:
  upload:
    avatar-dir: user-avatars      # 自定义头像目录名
    post-dir: community-posts     # 自定义帖子目录名
    other-dir: misc-files         # 自定义其他文件目录名
    organize-by-user: false       # 禁用按用户分目录
    organize-by-type: false       # 禁用按类型分目录
```

## 测试

### 运行单元测试

```bash
# 运行所有测试
mvn test

# 运行文件上传相关测试
mvn test -Dtest=FileUploadServiceTest

# 运行特定测试方法
mvn test -Dtest=FileUploadServiceTest#testUploadFile_Success
```

### 集成测试示例

```java
@SpringBootTest
@AutoConfigureMockMvc
class FileUploadIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testUploadAvatar_Authenticated() throws Exception {
        // 模拟认证用户
        String token = "mock-jwt-token";
        
        // 创建测试文件
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test-avatar.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );
        
        // 执行上传请求
        mockMvc.perform(MockMvcRequestBuilders
                .multipart("/api/v1/upload/avatar")
                .file(file)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.fileUrl").exists());
    }
}
```

## 注意事项

1. **生产环境配置**：
   - 修改JWT密钥为强密钥
   - 设置合适的文件大小限制
   - 配置合适的日志级别
   - 定期清理临时文件

2. **性能优化**：
   - 对于大文件上传，考虑分片上传
   - 启用文件MD5校验确保文件完整性
   - 配置合适的缓存策略

3. **安全建议**：
   - 定期审计上传的文件
   - 监控异常上传行为
   - 限制单个用户的每日上传次数
   - 对上传的文件进行病毒扫描

4. **存储管理**：
   - 定期备份重要文件
   - 监控磁盘使用情况
   - 实现文件生命周期管理
   - 考虑使用CDN加速文件访问

## 故障排除

### 常见问题

1. **文件上传失败，返回413错误**
   - 检查`max-file-size`和`max-request-size`配置
   - 检查Nginx/Apache等代理服务器的上传限制

2. **文件类型验证失败**
   - 检查`allowed-file-types`和`allowed-extensions`配置
   - 确保文件的MIME类型正确

3. **文件无法通过URL访问**
   - 检查`WebConfig`中的资源映射配置
   - 确保文件实际保存在指定目录
   - 检查文件权限

4. **删除文件失败**
   - 检查文件是否存在
   - 检查用户是否有删除权限
   - 检查文件是否被其他进程占用

### 日志查看

查看文件上传相关的日志：

```bash
# 查看应用日志
tail -f logs/application.log | grep "FileUpload"

# 查看特定用户的文件操作
tail -f logs/application.log | grep "用户 1"

# 查看错误日志
tail -f logs/application.log | grep "ERROR.*FileUpload"
```

## 版本历史

### v1.0.0 (2024-01-15)
- 初始版本发布
- 支持基本文件上传功能
- 支持图片文件类型验证
- 支持本地文件存储
- 完整的REST API接口
- 集成Spring Security认证
- 单元测试覆盖

## 支持与反馈

如有问题或建议，请：
1. 查看日志文件获取详细信息
2. 检查配置文件是否正确
3. 参考本文档的故障排除部分
4. 联系开发团队获取支持