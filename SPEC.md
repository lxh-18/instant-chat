# 在线聊天系统 - 系统规格说明书

## 1. 项目概述

- **项目名称**: InstantChat - 在线聊天系统
- **项目类型**: Spring Boot 后端 + JavaFX 桌面客户端
- **核心功能**: 私聊、群聊、好友管理、语音消息
- **目标用户**: 需要进行在线即时通讯的用户

## 2. 技术栈

### 后端
- Spring Boot 3.2.x
- Spring WebSocket (STOMP协议)
- Spring Security (JWT认证)
- MyBatis-Plus
- MySQL 8.0

### 客户端
- JavaFX 17
- Scene Builder (UI布局)

## 3. 数据库设计

### 3.1 用户表 (users)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| username | VARCHAR(50) | 用户名，唯一 |
| password | VARCHAR(255) | 密码（BCrypt加密） |
| nickname | VARCHAR(100) | 昵称 |
| avatar | VARCHAR(500) | 头像URL |
| status | TINYINT | 状态：0-离线 1-在线 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### 3.2 好友分组表 (friend_groups)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| user_id | BIGINT | 所属用户ID |
| group_name | VARCHAR(100) | 分组名称 |
| created_at | DATETIME | 创建时间 |

### 3.3 好友关系表 (friends)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| user_id | BIGINT | 用户ID |
| friend_id | BIGINT | 好友ID |
| group_id | BIGINT | 所属分组ID |
| created_at | DATETIME | 添加时间 |

### 3.4 好友请求表 (friend_requests)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| from_user_id | BIGINT | 发送者ID |
| to_user_id | BIGINT | 接收者ID |
| message | VARCHAR(255) | 验证消息 |
| status | TINYINT | 状态：0-待处理 1-已同意 2-已拒绝 3-已过期 |
| created_at | DATETIME | 发送时间 |
| expires_at | DATETIME | 过期时间 |

### 3.5 群组表 (chat_groups)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| group_name | VARCHAR(100) | 群名称 |
| group_avatar | VARCHAR(500) | 群头像 |
| owner_id | BIGINT | 群主ID |
| created_at | DATETIME | 创建时间 |

### 3.6 群成员表 (group_members)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| group_id | BIGINT | 群ID |
| user_id | BIGINT | 用户ID |
| joined_at | DATETIME | 加入时间 |

### 3.7 私聊消息表 (private_messages)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| from_user_id | BIGINT | 发送者ID |
| to_user_id | BIGINT | 接收者ID |
| content | TEXT | 消息内容 |
| message_type | TINYINT | 消息类型：1-文本 2-图片 3-语音 |
| file_path | VARCHAR(500) | 文件路径（语音/图片） |
| created_at | DATETIME | 发送时间 |
| is_read | TINYINT | 是否已读：0-未读 1-已读 |

### 3.8 群聊消息表 (group_messages)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| group_id | BIGINT | 群ID |
| from_user_id | BIGINT | 发送者ID |
| content | TEXT | 消息内容 |
| message_type | TINYINT | 消息类型：1-文本 2-图片 3-语音 |
| file_path | VARCHAR(500) | 文件路径 |
| created_at | DATETIME | 发送时间 |

## 4. API 设计

### 4.1 认证模块
- POST /api/auth/register - 用户注册
- POST /api/auth/login - 用户登录
- POST /api/auth/logout - 用户登出
- GET /api/auth/current - 获取当前用户信息

### 4.2 好友管理模块
- GET /api/friends - 获取好友列表
- GET /api/friends/groups - 获取好友分组列表
- POST /api/friends/groups - 创建好友分组
- PUT /api/friends/groups/{id} - 更新分组名称
- DELETE /api/friends/groups/{id} - 删除分组
- POST /api/friends/move - 移动好友到其他分组
- DELETE /api/friends/{friendId} - 删除好友
- POST /api/friend-requests - 发送好友请求
- GET /api/friend-requests - 获取好友请求列表
- PUT /api/friend-requests/{id} - 处理好友请求（同意/拒绝）
- POST /api/friend-requests/{id}/resend - 重新发送好友请求

### 4.3 聊天模块
- GET /api/messages/private/{friendId} - 获取私聊消息记录
- GET /api/messages/group/{groupId} - 获取群聊消息记录
- GET /api/messages/private/{friendId}/export - 导出私聊记录
- GET /api/messages/group/{groupId}/export - 导出群聊记录
- GET /api/groups - 获取用户加入的群组列表
- POST /api/groups - 创建群组
- POST /api/groups/{id}/join - 加入群组
- DELETE /api/groups/{id}/leave - 退出群组

### 4.4 WebSocket 端点
- /ws - WebSocket连接端点
- /topic/private/{userId} - 私聊消息订阅
- /topic/group/{groupId} - 群聊消息订阅
- /topic/friend-request/{userId} - 好友请求订阅
- /topic/user/status - 用户状态订阅

## 5. 功能模块

### 5.1 登录注册
- [x] 用户注册（用户名、密码、昵称、头像）
- [x] 用户登录（JWT Token认证）
- [x] 会话管理

### 5.2 好友管理
- [x] 好友分组管理（CRUD）
- [x] 移动好友到其他分组
- [x] 删除好友（可选删除聊天记录）
- [x] 好友请求（发送、接受、拒绝、重新发送）

### 5.3 私聊
- [x] 发送文本消息
- [x] 发送图片消息
- [x] 发送语音消息
- [x] 消息已读未读状态

### 5.4 群聊
- [x] 创建群组
- [x] 加入群组
- [x] 退出群组
- [x] 发送群消息

### 5.5 聊天记录
- [x] 查询历史消息
- [x] 导出为JSON格式
- [x] 导出为TXT格式

### 5.6 语音聊天
- [x] 录音并发送语音消息
- [x] 播放语音消息

## 6. 项目结构

```
Big-work/
├── backend/                    # Spring Boot 后端
│   └── instant-chat/
│       ├── src/main/java/com/instantchat/
│       │   ├── config/         # 配置类
│       │   ├── controller/    # 控制器
│       │   ├── service/       # 业务逻辑
│       │   ├── mapper/        # MyBatis映射
│       │   ├── entity/        # 实体类
│       │   ├── dto/          # 数据传输对象
│       │   ├── websocket/    # WebSocket配置
│       │   ├── security/     # 安全配置
│       │   └── utils/        # 工具类
│       └── src/main/resources/
│           └── application.yml
├── client/                    # JavaFX 客户端
│   └── src/main/java/com/instantchat/
│       ├── controller/       # FXML控制器
│       ├── model/            # 数据模型
│       ├── service/         # 网络服务
│       └── util/            # 工具类
└── sql/                      # 数据库脚本
    └── schema.sql
```

## 7. 启动说明

### 7.1 后端启动

1. **创建数据库**
```sql
CREATE DATABASE instant_chat DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. **执行建表SQL**
```bash
mysql -u root -p instant_chat < sql/schema.sql
```

3. **配置数据库连接**
编辑 `backend/instant-chat/src/main/resources/application.yml`，修改以下配置：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/instant_chat?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: your_username
    password: your_password
```

4. **启动后端**
```bash
cd backend/instant-chat
mvn spring-boot:run
```
或
```bash
mvn clean package
java -jar target/instant-chat-1.0.0.jar
```

后端启动成功后会运行在 http://localhost:8080

### 7.2 客户端说明

JavaFX客户端正在开发中，预计包含以下功能：
- 登录/注册界面
- 好友列表和分组管理
- 私聊和群聊界面
- 语音消息录制和播放
- 聊天记录导出

客户端源码将位于 `client/` 目录。
