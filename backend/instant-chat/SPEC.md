# 即时通讯系统 (Instant Chat) 需求文档

## 1. 项目概述

### 1.1 项目名称
Instant Chat 即时通讯系统

### 1.2 项目类型
基于 Spring Boot + WebSocket 的实时聊天后端服务，配合单页面前端实现完整即时通讯功能。

### 1.3 技术栈
| 类别 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.2.5 (Java 17) |
| 安全认证 | Spring Security + JWT (jjwt 0.12.5) |
| 实时通信 | Spring WebSocket + STOMP |
| ORM | MyBatis-Plus 3.5.6 |
| 数据库 | MySQL |
| 前端 | Bootstrap 5.3.0 + SockJS + STOMP.js |
| 构建工具 | Maven (WAR 打包，部署至 Tomcat) |

### 1.4 项目结构
```
src/main/java/com/instantchat/
├── InstantChatApplication.java     # 应用入口
├── config/                         # 配置类
│   ├── SecurityConfig.java         # Spring Security 配置
│   ├── WebSocketConfig.java        # WebSocket + STOMP 配置
│   ├── MyBatisPlusConfig.java      # MyBatis-Plus 自动填充配置
│   └── WebMvcConfig.java           # 静态资源映射配置
├── controller/                     # REST 控制器
│   ├── AuthController.java         # 认证接口
│   ├── UserSearchController.java   # 用户搜索接口
│   ├── FriendController.java       # 好友管理接口
│   ├── ChatController.java         # 聊天消息接口
│   ├── FileController.java         # 文件上传接口
│   └── MessageExportController.java # 消息导出接口
├── dto/                            # 数据传输对象
├── entity/                         # 数据库实体
├── mapper/                         # MyBatis-Plus Mapper
├── security/                       # JWT 安全过滤器
│   ├── JwtAuthFilter.java          # JWT 认证过滤器
│   └── JwtTokenProvider.java       # JWT 令牌提供者
├── service/                        # 业务逻辑层
│   ├── AuthService.java
│   ├── FriendService.java
│   ├── ChatService.java
│   ├── FileService.java
│   └── MessageExportService.java
├── utils/                          # 工具类
└── websocket/                      # WebSocket 处理器
    └── ChatWebSocketHandler.java   # STOMP 消息处理
```

---

## 2. 数据库设计

### 2.1 ER 关系图概述

- **User** 是核心实体，与多个实体有一对多关系
- **Friend** 实现用户间的双向好友关系
- **ChatGroup** 与 **GroupMember** 实现群组功能
- **PrivateMessage** 和 **GroupMessage** 分别存储私聊和群聊消息
- **FriendRequest** 和 **GroupInvitation** 处理邀请流程

### 2.2 数据表设计

#### 2.2.1 用户表 (users)
| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO | 用户ID |
| username | VARCHAR(50) | NOT NULL, UNIQUE | 用户名 |
| password | VARCHAR(255) | NOT NULL | BCrypt 加密密码 |
| nickname | VARCHAR(50) | NOT NULL | 昵称 |
| avatar | VARCHAR(255) | | 头像URL |
| status | TINYINT | DEFAULT 0 | 状态: 0=离线, 1=在线 |
| deleted | TINYINT | DEFAULT 0 | 逻辑删除标识 |
| created_at | DATETIME | | 创建时间 |
| updated_at | DATETIME | | 更新时间 |

#### 2.2.2 好友关系表 (friends)
| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO | 记录ID |
| user_id | BIGINT | FK | 用户ID |
| friend_id | BIGINT | FK | 好友用户ID |
| group_id | BIGINT | FK | 所属好友分组ID |

**约束**: 双向好友关系，即 (user_id=A, friend_id=B) 和 (user_id=B, friend_id=A) 两条记录

#### 2.2.3 好友分组表 (friend_groups)
| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO | 分组ID |
| user_id | BIGINT | FK | 所属用户ID |
| group_name | VARCHAR(50) | NOT NULL | 分组名称 |

#### 2.2.4 好友请求表 (friend_requests)
| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO | 请求ID |
| from_user_id | BIGINT | FK | 发送者ID |
| to_user_id | BIGINT | FK | 接收者ID |
| message | VARCHAR(255) | | 申请留言 |
| status | TINYINT | DEFAULT 0 | 状态: 0=待处理, 1=已接受, 2=已拒绝, 3=已过期 |
| expires_at | DATETIME | | 过期时间 |
| is_read | BOOLEAN | DEFAULT FALSE | 是否已读 |
| created_at | DATETIME | | 创建时间 |

**业务规则**: 好友请求默认7天后过期

#### 2.2.5 私聊消息表 (private_messages)
| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO | 消息ID |
| from_user_id | BIGINT | FK | 发送者ID |
| to_user_id | BIGINT | FK | 接收者ID |
| content | TEXT | | 消息内容 |
| message_type | TINYINT | DEFAULT 1 | 消息类型: 1=文本, 2=图片, 3=语音 |
| file_path | VARCHAR(255) | | 文件路径(图片/语音) |
| is_read | TINYINT | DEFAULT 0 | 已读标识: 0=未读, 1=已读 |
| created_at | DATETIME | | 发送时间 |

#### 2.2.6 群组表 (chat_groups)
| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO | 群组ID |
| group_name | VARCHAR(50) | NOT NULL | 群组名称 |
| group_avatar | VARCHAR(255) | | 群组头像 |
| owner_id | BIGINT | FK | 群主ID |
| created_at | DATETIME | | 创建时间 |

#### 2.2.7 群组成员表 (group_members)
| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO | 记录ID |
| group_id | BIGINT | FK | 群组ID |
| user_id | BIGINT | FK | 用户ID |
| joined_at | DATETIME | | 加入时间 |

#### 2.2.8 群组消息表 (group_messages)
| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO | 消息ID |
| group_id | BIGINT | FK | 群组ID |
| from_user_id | BIGINT | FK | 发送者ID |
| content | TEXT | | 消息内容 |
| message_type | TINYINT | DEFAULT 1 | 消息类型: 1=文本, 2=图片, 3=语音 |
| file_path | VARCHAR(255) | | 文件路径 |
| is_read | TINYINT | DEFAULT 0 | 已读标识 |
| created_at | DATETIME | | 发送时间 |

#### 2.2.9 群组邀请表 (group_invitations)
| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, AUTO | 邀请ID |
| group_id | BIGINT | FK | 群组ID |
| inviter_user_id | BIGINT | FK | 邀请人ID |
| invitee_user_id | BIGINT | FK | 被邀请人ID |
| message | VARCHAR(255) | | 邀请留言 |
| status | TINYINT | DEFAULT 0 | 状态: 0=待处理, 1=已接受, 2=已拒绝 |
| created_at | DATETIME | | 创建时间 |

---

## 3. 功能需求

### 3.1 认证模块

#### 3.1.1 用户注册
- **接口**: `POST /api/auth/register`
- **请求参数**:
  - username (必填, 唯一)
  - password (必填)
  - nickname (必填)
  - avatar (可选)
- **业务流程**:
  1. 校验用户名唯一性
  2. BCrypt 加密密码
  3. 创建用户记录
  4. 生成 JWT Token
  5. 返回 Token 和用户信息
- **响应**: `ApiResponse<AuthResponse>` 包含 token, userId, username, nickname, avatar

#### 3.1.2 用户登录
- **接口**: `POST /api/auth/login`
- **请求参数**: username, password
- **业务流程**:
  1. 验证用户名密码
  2. 更新用户状态为在线 (status=1)
  3. 生成 JWT Token
- **响应**: 同注册

#### 3.1.3 用户登出
- **接口**: `POST /api/auth/logout`
- **业务流程**: 更新用户状态为离线 (status=0)

#### 3.1.4 获取当前用户
- **接口**: `GET /api/auth/current`
- **响应**: 当前登录用户的完整信息

### 3.2 用户搜索模块

#### 3.2.1 搜索用户
- **接口**: `GET /api/users/search?keyword=xxx`
- **功能**: 按用户名或昵称模糊搜索，最多返回20条结果，排除当前用户

#### 3.2.2 获取用户详情
- **接口**: `GET /api/users/{id}`
- **响应**: 用户信息(密码字段隐藏)

### 3.3 好友管理模块

#### 3.3.1 好友分组管理
| 功能 | 接口 | 方法 | 说明 |
|------|------|------|------|
| 获取分组列表 | `GET /api/friends/groups` | GET | 获取当前用户的所有好友分组 |
| 创建分组 | `POST /api/friends/groups` | POST | 创建新的好友分组 |
| 重命名分组 | `PUT /api/friends/groups/{id}` | PUT | 修改分组名称 |
| 删除分组 | `DELETE /api/friends/groups/{id}` | DELETE | 删除分组，组内好友移至"未分组" |

#### 3.3.2 好友列表
- **接口**: `GET /api/friends`
- **响应**: 返回所有好友列表，包含用户信息、分组信息、在线状态

#### 3.3.3 移动好友
- **接口**: `POST /api/friends/move`
- **请求参数**: friendId, newGroupId 或 groupName
- **业务规则**: 若按 groupName 查找且分组不存在，自动创建新分组

#### 3.3.4 删除好友
- **接口**: `DELETE /api/friends/{friendId}?deleteMessages=boolean`
- **业务规则**:
  - 删除双向好友关系
  - 可选删除聊天记录

### 3.4 好友请求模块

#### 3.4.1 发送好友请求
- **接口**: `POST /api/friend-requests`
- **请求参数**: toUserId, message
- **业务规则**:
  - 有效期7天
  - 不能重复发送(同一对用户只能有一条待处理请求)
  - 发送后通过 WebSocket 实时通知接收者

#### 3.4.2 获取好友请求
- **接口**: `GET /api/friend-requests` (收到的请求)
- **接口**: `GET /api/friend-requests/sent` (发出的请求)

#### 3.4.3 处理好友请求
- **接口**: `PUT /api/friend-requests/{id}`
- **请求参数**: accept (boolean)
- **业务流程**:
  - 接受: 双向创建好友关系，通知双方
  - 拒绝: 更新请求状态
- **实时通知**: 处理结果通过 WebSocket 通知发送者

#### 3.4.4 重新发送请求
- **接口**: `POST /api/friend-requests/{id}/resend`
- **适用场景**: 之前被拒绝或已过期的请求

#### 3.4.5 标记已读
- **接口**: `POST /api/friend-requests/mark-read`

### 3.5 聊天消息模块

#### 3.5.1 私聊消息
| 功能 | 接口 | 方法 |
|------|------|------|
| 获取消息历史 | `GET /api/messages/private/{friendId}?page=&size=` | GET |
| 搜索消息 | `GET /api/messages/private/{friendId}/search?keyword=` | GET |
| 发送消息 | `POST /api/messages/private/{friendId}` | POST |
| 标记已读 | `POST /api/messages/private/{friendId}/mark-read` | POST |

#### 3.5.2 群聊消息
| 功能 | 接口 | 方法 |
|------|------|------|
| 获取消息历史 | `GET /api/messages/group/{groupId}?page=&size=` | GET |
| 搜索消息 | `GET /api/messages/group/{groupId}/search?keyword=` | GET |
| 发送消息 | `POST /api/messages/group/{groupId}` | POST |
| 标记已读 | `POST /api/messages/group/{groupId}/mark-read` | POST |

#### 3.5.3 未读消息统计
- **接口**: `GET /api/messages/unread-summary`
- **响应**: 各好友和群组的未读消息数量

### 3.6 群组管理模块

#### 3.6.1 群组 CRUD
| 功能 | 接口 | 方法 |
|------|------|------|
| 创建群组 | `POST /api/groups` | POST |
| 获取我的群组 | `GET /api/groups` | GET |
| 获取群组详情 | `GET /api/groups/{id}` | GET |
| 搜索群组 | `GET /api/groups/search?keyword=` | GET |
| 获取所有群组 | `GET /api/groups/all` | GET |

#### 3.6.2 群组成员管理
| 功能 | 接口 | 方法 |
|------|------|------|
| 加入群组 | `POST /api/groups/{id}/join` | POST |
| 邀请加入 | `POST /api/groups/{id}/invite` | POST |
| 获取成员列表 | `GET /api/groups/{id}/members` | GET |
| 退出群组 | `DELETE /api/groups/{id}/leave` | DELETE |
| **业务规则** | 群主不能退出群组 | |

### 3.7 文件上传模块

#### 3.7.1 上传文件
| 功能 | 接口 | 方法 | 文件大小限制 |
|------|------|------|-------------|
| 上传语音 | `POST /api/files/audio` | POST | 10MB |
| 上传图片 | `POST /api/files/image` | POST | 10MB |

#### 3.7.2 获取文件
- **接口**: `GET /api/files/{type}/{filename}`
- **type**: audio | image

### 3.8 消息导出模块

- **接口**:
  - `GET /api/messages/private/{friendId}/export/{format}` (format: json | txt)
  - `GET /api/messages/group/{groupId}/export/{format}`
- **导出格式**:
  - JSON: 结构化数据，便于程序处理
  - TXT: 可读性文本格式

---

## 4. WebSocket 实时通信设计

### 4.1 STOMP 端点配置
- **STOMP 端点**: `/ws`
- **消息代理前缀**: `/topic` (用于广播)
- **应用前缀**: `/app` (用于请求映射)

### 4.2 认证机制
- 在 STOMP CONNECT 帧的 `Authorization` 头传递 JWT Token
- 格式: `Bearer <token>`
- 连接成功后，用户ID作为 principal

### 4.3 实时消息主题

| 主题 | 用途 | 消息内容 |
|------|------|----------|
| `/topic/private/{userId}` | 私聊消息 | ChatMessageDto |
| `/topic/group/{groupId}` | 群聊消息 | ChatMessageDto |
| `/topic/friend-request/{userId}` | 好友请求通知 | FriendRequestDto |
| `/topic/user/{userId}/groups` | 群组邀请通知 | GroupInvitationDto |

### 4.4 STOMP 消息处理 (后端 @MessageMapping)

| 路径 | 用途 |
|------|------|
| `/app/chat/private` | 处理私聊消息发送 |
| `/app/chat/group` | 处理群聊消息发送 |

**说明**: 前端实际使用 REST API 发送消息，@MessageMapping 存在但未被前端调用

---

## 5. 安全设计

### 5.1 认证策略
- **JWT Token**:
  - 签名算法: HS256
  - 有效期: 7 天 (604800000 ms)
  - Token 格式: `userId-username` 作为 subject
- **密码加密**: BCrypt

### 5.2 接口权限配置

**无需认证的接口**:
- `/api/auth/login`
- `/api/auth/register`
- `/ws/**` (WebSocket STOMP 端点)
- `/index.html`
- `/static/**`
- `/audio/**`
- `/image/**`

**其他接口**: 均需认证

### 5.3 CORS 配置
- 允许所有来源 (`*`)
- 允许的方法: GET, POST, PUT, DELETE, OPTIONS
- 允许所有请求头

### 5.4 全局异常处理
| 异常类型 | HTTP 状态码 |
|----------|-------------|
| RuntimeException | 400 |
| 其他 Exception | 500 |

---

## 6. 前端页面设计

### 6.1 页面结构

#### 6.1.1 认证页
- 登录/注册表单切换
- 渐变背景

#### 6.1.2 聊天主页面
- **左侧边栏** (320px):
  - 好友列表 (按分组展示)
  - 群组列表
  - 好友请求入口
- **主聊天区域**:
  - 聊天对象信息栏
  - 消息列表 (气泡样式)
  - 消息输入框 (支持文本/图片/语音)
- **右侧滑出面板**:
  - 好友请求列表

### 6.2 功能组件

| 组件 | 功能 |
|------|------|
| 添加好友弹窗 | 搜索用户，发送好友请求 |
| 创建群组弹窗 | 创建新群组 |
| 加入群组弹窗 | 搜索/浏览群组，申请加入 |
| 邀请好友弹窗 | 选择好友邀请加入群组 |
| 分组管理弹窗 | 创建/重命名好友分组 |
| 搜索消息弹窗 | 搜索聊天记录 |
| 右键菜单 | 移动好友、查看资料、删除好友 |
| 录音界面 | 语音录制，显示计时器 |
| Toast 通知 | 顶部右侧消息提示 |

### 6.3 WebSocket 连接
- 连接地址: `${origin}/ws`
- 携带 Authorization 头
- 断开后自动重连 (3秒延迟)

### 6.4 媒体功能
- **语音录制**: MediaRecorder API，格式 WebM
- **图片选择**: 文件选择器

---

## 7. 配置参数

### 7.1 数据库配置
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/instant_chat
    username: root
    password: 123456
```

### 7.2 文件上传配置
```yaml
upload.path: D:/tomcat/webapps/Big-work/backend/instant-chat/uploads
spring.servlet.multipart:
  max-file-size: 10MB
  max-request-size: 10MB
```

### 7.3 JWT 配置
```yaml
jwt:
  secret: InstantChatSecretKey2024ForJWTTokenGenerationAndValidation
  expiration: 604800000  # 7天
```

### 7.4 静态资源映射
- `/audio/**` → `{upload.path}/audio/`
- `/image/**` → `{upload.path}/image/`

---

## 8. API 响应格式

### 8.1 统一响应结构 (ApiResponse<T>)
```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... }
}
```

### 8.2 响应码定义
| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未认证 |
| 403 | 无权限 |
| 500 | 服务器内部错误 |

---

## 9. 消息类型定义

| 类型值 | 消息类型 | 说明 |
|--------|----------|------|
| 1 | TEXT | 文本消息 |
| 2 | IMAGE | 图片消息 |
| 3 | AUDIO | 语音消息 |

---

## 10. 业务流程图

### 10.1 好友添加流程
```
用户A搜索用户B → A发送好友请求 → B收到通知 → B处理请求(接受/拒绝)
                                              ↓
                              接受: 双向创建好友关系
                              拒绝: 更新请求状态
```

### 10.2 私聊消息流程
```
A发送消息 → 保存到private_messages → WebSocket推送到 /topic/private/{B的userId}
                                            ↓
                              B端接收消息 → 更新未读数 → 显示Toast
```

### 10.3 群聊消息流程
```
A在群G发送消息 → 保存到group_messages → WebSocket推送到 /topic/group/{G的groupId}
                                            ↓
                              群内所有成员接收消息
```

### 10.4 群组邀请流程
```
群主A选择好友B → 发送邀请 → 保存到group_invitations → WebSocket推送到 /topic/user/{B的userId}
                                                                        ↓
                                                            B收到通知 → B处理邀请
```

---

## 11. 验收标准

### 11.1 认证模块
- [ ] 用户可以注册并获得 JWT Token
- [ ] 用户可以登录，登录后状态变为在线
- [ ] 用户可以登出，登出后状态变为离线
- [ ] Token 有效期为7天

### 11.2 好友模块
- [ ] 用户可以搜索其他用户
- [ ] 用户可以发送好友请求
- [ ] 用户可以接受/拒绝好友请求
- [ ] 好友请求被接受后，双方成为好友
- [ ] 用户可以创建/重命名/删除好友分组
- [ ] 用户可以将好友移动到不同分组
- [ ] 用户可以删除好友，可选删除聊天记录

### 11.3 聊天模块
- [ ] 用户可以发送和接收私聊消息
- [ ] 用户可以发送和接收群聊消息
- [ ] 消息支持文本、图片、语音三种类型
- [ ] 用户可以分页查看聊天历史
- [ ] 用户可以搜索聊天记录
- [ ] 用户可以标记消息为已读
- [ ] 用户可以查看未读消息数量

### 11.4 群组模块
- [ ] 用户可以创建群组并成为群主
- [ ] 用户可以申请加入群组
- [ ] 群主可以邀请好友加入群组
- [ ] 群主不能退出群组
- [ ] 普通成员可以退出群组
- [ ] 用户可以搜索群组

### 11.5 文件模块
- [ ] 用户可以上传语音文件
- [ ] 用户可以上传图片文件
- [ ] 文件大小限制为10MB
- [ ] 用户可以查看已上传的文件

### 11.6 导出模块
- [ ] 用户可以导出私聊记录为 JSON 格式
- [ ] 用户可以导出私聊记录为 TXT 格式
- [ ] 用户可以导出群聊记录为 JSON 格式
- [ ] 用户可以导出群聊记录为 TXT 格式

### 11.7 WebSocket 实时通信
- [ ] WebSocket 连接支持 JWT 认证
- [ ] 私聊消息实时推送
- [ ] 群聊消息实时推送
- [ ] 好友请求通知实时推送
- [ ] 群组邀请通知实时推送
- [ ] 连接断开后自动重连

### 11.8 前端界面
- [ ] 认证页面正常切换登录/注册
- [ ] 好友列表按分组展示
- [ ] 群组列表正常显示
- [ ] 聊天界面正常收发消息
- [ ] 消息气泡区分发送/接收
- [ ] 图片消息正确显示
- [ ] 语音消息可以播放
- [ ] 右键菜单功能正常
- [ ] Toast 通知正常显示
- [ ] 未读消息红点正常显示