# 帖子收藏接口文档（APP端对接）

## 通用说明

- **基础URL**: `https://your-domain.com/api/v1`
- **认证方式**: 所有收藏接口需要登录，请求头携带 `Authorization: Bearer <JWT_TOKEN>`
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

---

## 一、收藏帖子

收藏一个指定的帖子。

**请求**

```
POST /api/v1/posts/{postId}/favorite
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| postId | Long | 是 | 帖子ID，路径参数 |

**成功响应** (200)

```json
{
  "success": true,
  "message": "收藏成功",
  "data": null
}
```

**错误响应**

| HTTP状态码 | message | 说明 |
|-----------|---------|------|
| 400 | 已经收藏过此帖子 | 重复收藏 |
| 401 | 未登录或Token过期 | 需要重新登录 |
| 404 | 帖子不存在 | postId无效 |

---

## 二、取消收藏

取消对指定帖子的收藏。

**请求**

```
DELETE /api/v1/posts/{postId}/favorite
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| postId | Long | 是 | 帖子ID，路径参数 |

**成功响应** (200)

```json
{
  "success": true,
  "message": "取消收藏成功",
  "data": null
}
```

**错误响应**

| HTTP状态码 | message | 说明 |
|-----------|---------|------|
| 400 | 尚未收藏此帖子 | 未收藏却调用取消 |
| 401 | 未登录或Token过期 | 需要重新登录 |

---

## 三、检查是否收藏

检查当前用户是否已收藏指定帖子。

**请求**

```
GET /api/v1/posts/{postId}/is-favorited
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| postId | Long | 是 | 帖子ID，路径参数 |

**成功响应** (200)

```json
{
  "success": true,
  "message": "检查成功",
  "data": true
}
```

`data` 为 `boolean` 类型，`true` 表示已收藏，`false` 表示未收藏。

---

## 四、获取我的收藏列表

获取当前用户收藏的帖子列表，按收藏时间倒序排列，支持分页。

**请求**

```
GET /api/v1/posts/favorites?page=0&size=20
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
        "favorited": true,
        "followed": false,
        "followMe": false
      }
    ],
    "total": 25,
    "page": 0,
    "size": 20,
    "totalPages": 2
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
| status | Integer | 0=草稿，1=已发布，2=已删除 |
| faction | String | 门派 |
| region | String | 大区 |
| server | String | 服务器 |
| bodyType | String | 体型 |
| gameplay | String | 玩法 |
| imageUrlList | List\<String\> | 图片列表（已解析） |
| tagList | List\<String\> | 标签列表（已解析） |
| author | User | 作者简要信息 |
| liked | Boolean | 当前用户是否点赞 |
| favorited | Boolean | 当前用户是否收藏（收藏列表固定为true） |
| followed | Boolean | 当前用户是否关注了作者 |

---

## 五、帖子详情/列表中的收藏状态

在获取帖子详情、帖子列表等接口的返回数据中，`post` 对象已新增 `favoriteCount`（收藏数）和 `favorited`（当前用户是否收藏）字段。

**涉及接口**

- `GET /api/v1/posts/{postId}` — 帖子详情
- `GET /api/v1/posts` — 帖子列表
- `GET /api/v1/posts/following` — 关注用户帖子
- `GET /api/v1/posts/by-tag/{tagName}` — 按标签查询帖子
- `GET /api/v1/posts/user/{userId}` — 用户帖子
- `GET /api/v1/posts/favorites` — 收藏列表

---

## 六、APP端交互建议

### 6.1 收藏按钮
- 进入帖子详情页时，根据 `favorited` 字段显示收藏/已收藏图标
- 点击收藏按钮调用 `POST /favorite`，成功后切换为已收藏状态
- 再次点击调用 `DELETE /favorite`，成功后切换为未收藏状态
- 请求期间按钮置灰防止重复点击

### 6.2 收藏列表
- "我的收藏"页面调用 `GET /favorites`，支持下拉加载更多（page递增）
- 无数据时展示空状态提示"暂无收藏"
- 列表项展示：封面图、标题、作者昵称、收藏时间（按收藏时间倒序）

### 6.3 错误处理
- 401 错误：引导用户重新登录
- 400 "已经收藏过此帖子"：通常不会出现（按钮状态控制），做兜底处理
- 网络异常：Toast提示"网络异常，请稍后再试"并恢复按钮状态

### 6.4 乐观更新
建议采用乐观更新策略提升体验：
- 点击收藏时，立即切换UI状态
- 请求失败时回滚状态并提示用户
