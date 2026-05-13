# 私聊系统对接文档

## 概述

后端实现了基于 WebSocket + STOMP 协议的 1v1 私聊功能，支持实时消息推送、会话管理、消息撤回、已读功能。

---

## 一、连接方式

### 1. WebSocket 连接地址

```
ws://{服务器地址}/ws
```

例如：`ws://localhost:8080/ws`

### 2. 连接认证

连接时需要在 STOMP CONNECT 帧的 header 中传递 JWT token：

```
CONNECT
Authorization: Bearer {access_token}

^@
```

如果认证失败，服务端会断开连接。

### 3. 连接心跳（可选）

建议客户端每 30 秒发送一次心跳（通过 STOMP SEND 到 `/app/heartbeat`），保持连接活跃。

---

## 二、STOMP 消息格式

### 1. 客户端发送消息

**发送到 `/app/chat.send`**

```json
{
    "conversationId": 123,
    "content": "你好",
    "type": 1
}
```

字段说明：
- `conversationId`: 会话ID（必填）
- `content`: 消息内容（必填，最大5000字符）
- `type`: 消息类型（可选，默认1）
  - `1`: 文本消息
  - `2`: 图片消息

**SEND 帧格式：**

```
SEND
destination:/app/chat.send
content-type:application/json

{"conversationId":123,"content":"你好","type":1}
^@
```

### 2. 服务端主动推送

#### 新消息推送

**订阅 `/user/queue/messages`**

服务端会推送新消息到这个订阅地址：

```json
{
    "id": 456,
    "conversationId": 123,
    "senderId": 789,
    "content": "你好",
    "type": 1,
    "status": 1,
    "createdAt": "2026-04-26T10:30:00",
    "sender": {
        "id": 789,
        "username": "user1",
        "nickname": "张三",
        "avatarUrl": "https://example.com/avatar.jpg"
    }
}
```

#### 系统通知

**订阅 `/user/queue/system`**

推送系统通知，如消息撤回：

```json
{
    "type": "message_recall",
    "messageId": 456
}
```

---

## 三、REST 接口

以下接口均需要携带 JWT token（通过 `Authorization: Bearer {token}` header）。

### 1. 创建或获取会话

```
POST /api/v1/chat/conversations
Content-Type: application/json
Authorization: Bearer {access_token}

{
    "otherUserId": 123
}
```

**响应：**

```json
{
    "success": true,
    "message": "会话创建成功",
    "data": {
        "id": 1,
        "user1Id": 100,
        "user2Id": 123,
        "otherUser": {
            "id": 123,
            "username": "user123",
            "nickname": "李四",
            "avatarUrl": "https://example.com/avatar.jpg"
        },
        "unreadCount": 0,
        "createdAt": "2026-04-26T10:00:00"
    }
}
```

### 2. 获取会话列表

```
GET /api/v1/chat/conversations
Authorization: Bearer {access_token}
```

**响应：**

```json
{
    "success": true,
    "message": "获取成功",
    "data": [
        {
            "id": 1,
            "user1Id": 100,
            "user2Id": 123,
            "lastMessage": {
                "id": 456,
                "content": "你好",
                "type": 1,
                "senderId": 123,
                "createdAt": "2026-04-26T10:30:00"
            },
            "otherUser": {
                "id": 123,
                "username": "user123",
                "nickname": "李四",
                "avatarUrl": "https://example.com/avatar.jpg"
            },
            "unreadCount": 2,
            "lastMessageAt": "2026-04-26T10:30:00"
        }
    ]
}
```

### 3. 获取会话消息

```
GET /api/v1/chat/conversations/{conversationId}/messages?page=0&size=20
Authorization: Bearer {access_token}
```

**响应：**

```json
{
    "success": true,
    "message": "获取成功",
    "data": {
        "content": [
            {
                "id": 456,
                "conversationId": 1,
                "senderId": 123,
                "content": "你好",
                "type": 1,
                "status": 1,
                "createdAt": "2026-04-26T10:30:00",
                "sender": {
                    "id": 123,
                    "username": "user123",
                    "nickname": "李四",
                    "avatarUrl": "https://example.com/avatar.jpg"
                }
            }
        ],
        "total": 50,
        "page": 0,
        "size": 20,
        "totalPages": 3
    }
}
```

### 4. 发送消息（HTTP 备用）

如果 WebSocket 不可用，可通过 HTTP 接口发送消息：

```
POST /api/v1/chat/conversations/{conversationId}/messages
Content-Type: application/json
Authorization: Bearer {access_token}

{
    "content": "你好",
    "type": 1
}
```

### 5. 直接发送消息给用户（自动创建会话）

```
POST /api/v1/chat/send?receiverId=123
Content-Type: application/json
Authorization: Bearer {access_token}

{
    "content": "你好",
    "type": 1
}
```

### 6. 撤回消息

```
DELETE /api/v1/chat/messages/{messageId}
Authorization: Bearer {access_token}
```

注意：消息发送后 2 分钟内可撤回，超过 2 分钟将返回错误。

### 7. 标记已读

```
PUT /api/v1/chat/conversations/{conversationId}/read
Authorization: Bearer {access_token}
```

### 8. 获取未读数

```
GET /api/v1/chat/conversations/{conversationId}/unread
Authorization: Bearer {access_token}
```

### 9. 获取总未读数

```
GET /api/v1/chat/unread
Authorization: Bearer {access_token}
```

---

## 四、消息流程示例

### 场景：用户A向用户B发送消息

1. **用户A连接 WebSocket**

```
CONNECT
Authorization: Bearer {userA_token}
^@
```

收到心跳：
```
CONNECTED
version:1.2
heart-beat:10000,10000
^@
```

2. **用户A订阅消息队列**

```
SUBSCRIBE
id:sub-1
destination:/user/queue/messages
^@
```

3. **用户A发送消息**

```
SEND
destination:/app/chat.send
content-type:application/json

{"conversationId":1,"content":"你好","type":1}
^@
```

4. **用户A收到服务器确认**

服务器通过 `/app/chat.send` 路由返回发送的消息对象（可选）。

5. **用户B收到实时推送**

用户B通过 `/user/queue/messages` 订阅收到新消息推送。

6. **用户B标记已读**

用户B打开对话，调用 `PUT /api/v1/chat/conversations/1/read`，用户A的未读数减少。

---

## 五、错误处理

### WebSocket 连接失败

如果 WebSocket 连接失败，客户端应该自动降级到 HTTP 接口发送消息。

### 消息发送失败

如果消息发送失败（网络问题等），客户端应保留消息本地缓存，并在恢复后重试发送。

### 认证失败

如果 token 过期，需要重新登录获取新的 token，并重新连接 WebSocket。

---

## 六、字段说明

### Conversation（会话）

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | Long | 会话ID |
| `user1Id` | Long | 用户1的ID（较小者） |
| `user2Id` | Long | 用户2的ID（较大者） |
| `otherUser` | User | 当前用户看到的对方用户信息 |
| `lastMessage` | Message | 最后一条消息（可选） |
| `lastMessageAt` | LocalDateTime | 最后消息时间 |
| `unreadCount` | Integer | 当前用户的未读消息数 |
| `createdAt` | LocalDateTime | 会话创建时间 |

### Message（消息）

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | Long | 消息ID |
| `conversationId` | Long | 所属会话ID |
| `senderId` | Long | 发送者ID |
| `content` | String | 消息内容 |
| `type` | Integer | 消息类型：1-文本，2-图片 |
| `status` | Integer | 状态：1-正常，0-已撤回 |
| `createdAt` | LocalDateTime | 发送时间 |
| `sender` | User | 发送者信息 |

### User（用户）

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | Long | 用户ID |
| `username` | String | 用户名 |
| `nickname` | String | 昵称 |
| `avatarUrl` | String | 头像URL |