# 帖子浏览历史接口文档（APP端对接）

## 通用说明

- **基础URL**: `https://your-domain.com/api/v1`
- **认证方式**: 所有浏览历史接口需要登录，请求头携带 `Authorization: Bearer <JWT_TOKEN>`
- **响应格式**: 统一使用 `ApiResponse<T>` 包裹

```json
{
  "success": true,
  "message": "操作成功",
  "data": { ... },
  "timestamp": "2026-05-07T12:00:00"
}
```

- **分页响应格式**: 分页接口 data 为 `PagedResult<T>`

```json
{
  "success": true,
  "message": "获取成功",
  "data": {
    "content": [ ... ],
    "total": 100,
    "page": 0,
    "size": 20,
    "totalPages": 5
  }
}
```

- **容量限制**: 每个用户最多保留 200 条浏览记录，超出后自动清理最旧的记录
- **去重逻辑**: 同一帖子重复浏览时，更新浏览时间并移到列表最前面，不会产生重复记录

---

## 一、获取浏览历史

获取当前用户的帖子浏览历史列表，按浏览时间倒序排列，支持分页。

**请求**

```
GET /api/v1/posts/browsing-history?page=0&size=20
```

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Integer | 否 | 0 | 页码，从0开始 |
| size | Integer | 否 | 20 | 每页大小 |

**成功响应** (200)

```json
{
  "success": true,
  "message": "获取成功",
  "data": {
    "content": [
      {
        "id": 1,
        "userId": 2,
        "title": "帖子标题",
        "content": "帖子正文...",
        "summary": "摘要",
        "coverImageUrl": "https://...",
        "viewCount": 100,
        "likeCount": 10,
        "favoriteCount": 5,
        "commentCount": 3,
        "status": 1,
        "visibility": 1,
        "isTop": false,
        "publishedAt": "2026-05-07T10:00:00",
        "createdAt": "2026-05-07T10:00:00",
        "updatedAt": "2026-05-07T10:00:00",
        "faction": "纯阳",
        "region": "电信区",
        "server": "双梦",
        "bodyType": "成男",
        "gameplay": "PVP",
        "target": "找队友",
        "contactDetail": "QQ:123456",
        "imageUrls": "[\"https://img1.jpg\",\"https://img2.jpg\"]",
        "tags": "[\"找亲友\",\"日常\"]",
        "imageUrlList": ["https://img1.jpg", "https://img2.jpg"],
        "tagList": ["找亲友", "日常"],
        "author": {
          "id": 2,
          "username": "user1",
          "nickname": "冰淇淋爱好者",
          "avatarUrl": "https://..."
        },
        "liked": true,
        "favorited": false,
        "followed": false,
        "followMe": false
      }
    ],
    "total": 50,
    "page": 0,
    "size": 20,
    "totalPages": 3
  }
}
```

**帖子对象关键字段说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 帖子ID |
| userId | Long | 发布者用户ID |
| title | String | 标题 |
| content | String | 正文内容 |
| summary | String | 摘要（可能为null） |
| coverImageUrl | String | 封面图URL（可能为null） |
| viewCount | Integer | 浏览数 |
| likeCount | Integer | 点赞数 |
| favoriteCount | Integer | 收藏数 |
| commentCount | Integer | 评论数 |
| faction | String | 门派 |
| region | String | 大区 |
| server | String | 服务器 |
| bodyType | String | 体型 |
| gameplay | String | 玩法 |
| imageUrlList | List\<String\> | 图片列表（已解析） |
| tagList | List\<String\> | 标签列表（已解析） |
| author | User | 作者简要信息（id, username, nickname, avatarUrl） |
| liked | Boolean | 当前用户是否点赞 |
| favorited | Boolean | 当前用户是否收藏 |
| followed | Boolean | 当前用户是否关注了作者 |
| followMe | Boolean | 作者是否关注了当前用户 |

> **注意**: 列表按浏览时间倒序排列（最近浏览的排最前）。createdAt 字段在浏览历史接口中始终为帖子的创建时间，实际的浏览时间目前不单独返回，排序已由后端保证。

---

## 二、清除浏览历史

清除当前用户的全部浏览历史记录。

**请求**

```
DELETE /api/v1/posts/browsing-history
```

无请求参数。

**成功响应** (200)

```json
{
  "success": true,
  "message": "清除成功",
  "data": null
}
```

**错误响应**

| HTTP状态码 | message | 说明 |
|-----------|---------|------|
| 401 | 未登录或Token过期 | 需要重新登录 |

---

## 三、浏览历史自动记录

浏览历史会在以下场景自动记录：

- 调用 `GET /api/v1/posts/{postId}` 查看帖子详情时，自动记录浏览历史
- 同一帖子多次浏览只保留最新一次记录（更新时间戳，移到列表最前）
- 最多保留 200 条，超出后自动清除最旧的记录

---

## 四、APP端交互建议

### 4.1 浏览历史列表页
- "浏览历史"页面调用 `GET /browsing-history`，支持下拉加载更多（page递增）
- 列表项展示：封面图、标题、作者昵称、标签
- 页面顶部提供"清除历史"按钮，点击弹出确认对话框后调用 `DELETE /browsing-history`
- 无数据时展示空状态提示"暂无浏览记录"

### 4.2 清除历史
- 点击"清除历史"按钮后弹出确认对话框："确定要清除所有浏览记录吗？"
- 确认后调用 DELETE 接口，成功后清空列表并展示空状态
- 清除操作不可逆，建议做二次确认

### 4.3 错误处理
- 401 错误：引导用户重新登录
- 网络异常：Toast提示"网络异常，请稍后再试"

### 4.4 隐私说明
- 浏览历史仅保存在本地服务器，不会与其他用户共享
- 用户可随时清除自己的浏览历史
