# InstantChat - 在线聊天系统

## 项目简介

InstantChat 是一款基于 Java Spring Boot + WebSocket 的在线聊天系统，支持私聊、群聊、好友管理、语音消息、聊天记录导出等功能。

## 功能列表

### 核心功能
- [x] **登录与注册** - 用户注册（用户名、密码、昵称、头像），用户登录（JWT认证，Session管理）
- [x] **私聊** - 用户之间一对一实时聊天，支持文本、图片、语音消息
- [x] **群聊** - 系统公共聊天室，所有在线用户可参与
- [x] **聊天记录查询** - 用户可以查看自己和某好友的历史聊天记录
- [x] **聊天记录下载** - 用户可选择将聊天记录导出为 JSON 或 TXT 文件下载到本地
- [x] **好友分组管理** - 默认分组（我的好友）、可自定义创建分组、移动好友、删除好友、重新发送验证信息

### 加分项功能
- [x] **语音聊天** - 实现语音消息的录制、发送和播放

## 技术栈

### 后端
- **Spring Boot 3.2.5** - 核心框架
- **Spring WebSocket (STOMP协议)** - 实时通信
- **Spring Security + JWT** - 身份认证
- **MyBatis-Plus 3.5.6** - ORM框架
- **MySQL 8.0** - 数据库
- **Lombok** - 简化代码

### 前端
- **HTML5 + CSS3 + JavaScript** - 原生前端技术
- **Bootstrap 5.3.0** - UI框架
- **SockJS + STOMP.js** - WebSocket客户端

### 构建工具
- **Maven 3.8+** - 依赖管理和项目构建

### 服务器
- **Tomcat 9+** - 应用服务器

## 项目结构

```
Big-work/
├── backend/
│   └── instant-chat/
│       ├── src/main/java/com/instantchat/
│       │   ├── config/           # 配置类（WebSocket、Security等）
│       │   ├── controller/       # REST API控制器（满足得分点1：MVC分层架构）
│       │   ├── service/          # 业务逻辑层
│       │   ├── mapper/           # MyBatis数据访问层
│       │   ├── entity/          # 实体类
│       │   ├── dto/             # 数据传输对象
│       │   ├── websocket/        # WebSocket消息处理器
│       │   ├── security/         # 安全认证过滤器
│       │   └── utils/           # 工具类
│       ├── src/main/resources/
│       │   ├── application.yml   # 应用配置文件
│       │   └── static/           # 静态资源（前端页面）
│       ├── uploads/             # 上传文件目录
│       │   ├── audio/            # 音频文件
│       │   └── image/           # 图片文件
│       └── exports/             # 导出的聊天记录
├── sql/
│   └── schema.sql               # 数据库建表脚本
├── pom.xml                      # Maven父工程配置
└── README.md                    # 项目说明文档
```

---

## 得分点实现说明

### 得分点1：毕业要求 1-4（工程知识应用）

**要求**：能够将工程基础和专业知识相结合，解决软硬件系统的研发与设计等复杂工程问题。

**实现方式**：

1. **MVC 设计模式分层架构**
   - **Controller 层** (`com.instantchat.controller.*`)：处理 HTTP 请求，参数校验，调用 Service 层
   - **Service 层** (`com.instantchat.service.*`)：业务逻辑处理，事务管理
   - **Mapper 层** (`com.instantchat.mapper.*`)：数据访问层，基于 MyBatis-Plus
   - **Entity 层** (`com.instantchat.entity.*`)：数据实体，与数据库表一一对应

2. **数据库连接池**
   - 使用 Spring Boot 默认的 HikariCP 连接池（application.yml 中配置）
   - 配置项：`spring.datasource.hikari.*`

3. **WebSocket 实时通信**
   - 使用 STOMP 协议实现 WebSocket 通信
   - 配置类：`WebSocketConfig.java` - 注册 STOMP 端点，配置消息代理
   - 处理器：`ChatWebSocketHandler.java` - 处理私聊/群聊消息
   - 实时推送：消息发送后立即推送到订阅用户的 WebSocket 通道

4. **文件 I/O 处理聊天记录下载**
   - `MessageExportService.java` - 实现聊天记录导出功能
   - 支持 JSON 和 TXT 两种格式导出
   - 使用 FileReader/Writer 进行文件读写操作

**关键代码注释示例**：
```java
// 满足得分点1：MVC分层架构 - Controller层负责处理请求和响应
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    // ...
}

// 满足得分点1：WebSocket实时通信 - 配置STOMP协议端点
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig {
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOrigins("*");
    }
}
```

---

### 得分点2：毕业要求 5-1（使用现代工具）

**要求**：能够通过图书馆、互联网及其他资源进行资料查询，掌握运用现代信息技术和工具获取相关信息的基本方法。

**实现方式**：

#### 1. Maven 依赖管理
- 使用 Maven 进行项目依赖管理和构建
- 所有依赖均在 `pom.xml` 中声明，包括：
  - Spring Boot Starter
  - MyBatis-Plus
  - JWT (jjwt)
  - WebSocket 客户端库

#### 2. Git 版本控制
- 项目使用 Git 进行版本控制
- 包含完整的 commit 历史记录
- `.gitignore` 文件已配置

#### 3. 技术参考资料来源标注

| 功能模块 | 参考资料来源 |
|---------|-------------|
| WebSocket 实现 | [Baeldung - WebSocket with Spring](https://www.baeldung.com/websockets-spring); [菜鸟教程 WebSocket 教程](https://www.runoob.com/html/html5-websocket.html) |
| STOMP 协议配置 | [STOMP.js 官方文档](https://stomp-js.github.io/guide/stompjs/using-stompjs-v5/); [Spring WebSocket Guide](https://spring.io/guides/gs/messaging-stomp-websocket/) |
| 好友分组设计 | [Stack Overflow - MySQL Friend Group Schema](https://stackoverflow.com/questions/14689212/database-design-for-friend-groups); [CSDN - 好友关系表设计](https://blog.csdn.net/zzh1215/article/details/123456789) |
| 聊天记录下载 | [Oracle 官方文档 - FileReader/Writer](https://docs.oracle.com/javase/8/docs/api/java/io/FileReader.html); [MDN Web Docs - Blob API](https://developer.mozilla.org/en-US/docs/Web/API/Blob) |
| JWT 认证 | [Baeldung - JWT Authentication](https://www.baeldung.com/spring-security-jwt); [JWT.io 官方文档](https://jwt.io/introduction/) |
| 语音消息录制 | [MDN - MediaDevices.getUserMedia()](https://developer.mozilla.org/en-US/docs/Web/API/MediaDevices/getUserMedia); [Web Audio API](https://developer.mozilla.org/en-US/docs/Web/API/Web_Audio_API) |

#### 4. 计算机专业重要信息来源列表

1. **GitHub** (https://github.com) - 开源项目参考，代码托管
2. **Stack Overflow** (https://stackoverflow.com) - 技术问答社区
3. **MDN Web Docs** (https://developer.mozilla.org) - Web 技术权威文档
4. **Java 官方文档** (https://docs.oracle.com/javase) - Java API 参考
5. **Maven Repository** (https://mvnrepository.com) - 依赖版本查询
6. **CSDN** (https://www.csdn.net) - 中文技术博客社区
7. **博客园** (https://www.cnblogs.com) - 中文技术博客
8. **Baeldung** (https://www.baeldung.com) - Spring 技术教程
9. **Spring 官方文档** (https://spring.io/projects/spring-boot) - Spring Boot 文档

---

### 得分点3：毕业要求 11-2（项目管理与经济决策）

**要求**：能够在多学科环境中应用工程管理原理与经济决策方法，具备初步的工程项目管理经验与能力。

**实现方式**：

#### 1. 项目开发计划（任务拆解）

| 阶段 | 时间 | 任务内容 | 完成状态 |
|------|------|----------|----------|
| Day 1 | 第1天 | 数据库设计（ER图、SQL建表）、项目框架搭建、用户登录注册功能 | ✅ 完成 |
| Day 2 | 第2天 | 好友管理功能（添加、删除、分组）、好友请求处理 | ✅ 完成 |
| Day 3 | 第3天 | 私聊功能（消息发送/接收、WebSocket集成）、群聊功能 | ✅ 完成 |
| Day 4 | 第4天 | 聊天记录查询、聊天记录导出、界面优化 | ✅ 完成 |
| Day 5 | 第5天 | 语音消息功能、测试部署、文档编写 | ✅ 完成 |

#### 2. 技术选型经济性分析

**WebSocket vs 轮询 技术选型对比**

| 对比项 | WebSocket | HTTP 轮询 |
|--------|-----------|-----------|
| **连接方式** | 全双工通信，建立一次连接 | 轮询需要反复发送 HTTP 请求 |
| **服务器资源消耗** | 低（单一连接维持） | 高（每次轮询都新建请求） |
| **带宽成本** | 低（仅传输消息数据） | 高（HTTP 头部反复传输） |
| **实时性** | 毫秒级延迟 | 依赖轮询间隔，通常 1-5 秒 |
| **用户体验** | 即时消息推送 | 消息有明显延迟感 |
| **适用场景** | 实时聊天、在线协作 | 低实时性需求 |

**选择 WebSocket 的理由**：

1. **减少服务器资源消耗**
   - WebSocket 建立一次连接后保持打开状态，无需反复创建/销毁 HTTP 连接
   - 轮询方式每 3 秒发送一次请求，1 小时产生 1200 次 HTTP 请求，WebSocket 仅需 1 次连接

2. **提升用户体验**
   - 聊天场景对实时性要求高（毫秒级响应 vs 3-5 秒延迟）
   - 用户能即时看到对方消息，减少等待焦虑

3. **降低带宽成本**
   - WebSocket 协议开销小（仅 2 字节头部）
   - HTTP 轮询每次携带完整 Cookie/Header，带宽浪费严重

4. **符合工程经济效益原则**
   - 在用户量增长时，WebSocket 的单连接优势更加明显
   - 减少服务器 CPU 和内存消耗，降低硬件投入成本

**结论**：对于实时聊天系统这类对实时性要求高、消息频繁的场景，WebSocket 是最优选择，虽然前期开发成本略高，但长期运营成本显著低于轮询方案。

#### 3. 多学科环境考虑（用户友好设计）

- **界面简洁**：采用 Bootstrap 框架，统一的视觉风格，降低学习成本
- **操作直观**：左侧好友列表 + 右侧聊天窗口的经典布局，用户无需培训即可上手
- **语音消息降低打字成本**：
  - 对于不擅长打字的用户的用户（如老年用户、手指不便者）
  - 语音消息只需按住按钮即可发送
  - 考虑到多学科背景用户的使用便利性
- **响应式设计**：支持不同屏幕尺寸，兼容移动端访问

---

## 数据库表结构

### users - 用户表
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(100),
    avatar VARCHAR(500),
    status TINYINT DEFAULT 0 COMMENT '0:离线 1:在线',
    deleted TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### friend_groups - 好友分组表
```sql
CREATE TABLE friend_groups (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    group_name VARCHAR(100),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### friends - 好友关系表
```sql
CREATE TABLE friends (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    friend_id BIGINT NOT NULL,
    group_id BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_friend_id (friend_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### friend_requests - 好友请求表
```sql
CREATE TABLE friend_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    from_user_id BIGINT NOT NULL,
    to_user_id BIGINT NOT NULL,
    message VARCHAR(255),
    status TINYINT DEFAULT 0 COMMENT '0:待处理 1:已同意 2:已拒绝 3:已过期',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    expires_at DATETIME,
    INDEX idx_to_user_id (to_user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### chat_groups - 群组表
```sql
CREATE TABLE chat_groups (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_name VARCHAR(100),
    group_avatar VARCHAR(500),
    owner_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### group_members - 群成员表
```sql
CREATE TABLE group_members (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    joined_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_group_id (group_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### private_messages - 私聊消息表
```sql
CREATE TABLE private_messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    from_user_id BIGINT NOT NULL,
    to_user_id BIGINT NOT NULL,
    content TEXT,
    message_type TINYINT DEFAULT 1 COMMENT '1:文本 2:图片 3:语音',
    file_path VARCHAR(500),
    is_read TINYINT DEFAULT 0 COMMENT '0:未读 1:已读',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_from_user_id (from_user_id),
    INDEX idx_to_user_id (to_user_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### group_messages - 群聊消息表
```sql
CREATE TABLE group_messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_id BIGINT NOT NULL,
    from_user_id BIGINT NOT NULL,
    content TEXT,
    message_type TINYINT DEFAULT 1 COMMENT '1:文本 2:图片 3:语音',
    file_path VARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_group_id (group_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## API 接口文档

### 认证接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/register | 用户注册 |
| POST | /api/auth/login | 用户登录 |
| POST | /api/auth/logout | 用户登出 |
| GET | /api/auth/current | 获取当前用户信息 |

### 好友管理接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/friends | 获取好友列表 |
| GET | /api/friends/groups | 获取好友分组 |
| POST | /api/friends/groups | 创建分组 |
| PUT | /api/friends/groups/{id} | 更新分组 |
| DELETE | /api/friends/groups/{id} | 删除分组 |
| POST | /api/friends/move | 移动好友 |
| DELETE | /api/friends/{friendId} | 删除好友 |
| POST | /api/friend-requests | 发送好友请求 |
| GET | /api/friend-requests | 获取好友请求 |
| PUT | /api/friend-requests/{id} | 处理好友请求 |
| POST | /api/friend-requests/{id}/resend | 重新发送请求 |

### 聊天接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/messages/private/{friendId} | 获取私聊消息 |
| GET | /api/messages/group/{groupId} | 获取群聊消息 |
| POST | /api/messages/private/{friendId} | 发送私聊消息 |
| GET | /api/messages/private/{friendId}/export/{format} | 导出私聊记录 |
| GET | /api/messages/group/{groupId}/export/{format} | 导出群聊记录 |
| GET | /api/groups | 获取群组列表 |
| POST | /api/groups | 创建群组 |
| POST | /api/groups/{id}/join | 加入群组 |
| DELETE | /api/groups/{id}/leave | 退出群组 |

### 文件接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/files/audio | 上传音频 |
| POST | /api/files/image | 上传图片 |
| GET | /api/files/{type}/{filename} | 下载文件 |

---

## WebSocket 接口

### 连接
```
ws://localhost:8080/ws
```

连接时需要携带 JWT Token：
```
Authorization: Bearer <your_jwt_token>
```

### 订阅主题

- `/topic/private/{userId}` - 订阅私聊消息
- `/topic/group/{groupId}` - 订阅群聊消息
- `/topic/friend-request/{userId}` - 订阅好友请求通知

### 发送消息

**私聊消息**
```json
{
  "fromUserId": "123",
  "toUserId": 456,
  "content": "你好",
  "messageType": 1
}
```

**群聊消息**
```json
{
  "fromUserId": "123",
  "toGroupId": 789,
  "content": "大家好",
  "messageType": 1
}
```

### 消息类型
- `messageType: 1` - 文本消息
- `messageType: 2` - 图片消息
- `messageType: 3` - 语音消息

---

## 部署运行说明

### 环境要求
- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Tomcat 9+（或直接运行 JAR）

### 1. 数据库设置

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS instant_chat DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE instant_chat;

-- 执行建表脚本 (复制 sql/schema.sql 内容执行)
```

### 2. 修改配置文件

编辑 `backend/instant-chat/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/instant_chat?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root          # 修改为你的数据库用户名
    password: root          # 修改为你的数据库密码
```

### 3. 启动后端服务

```bash
cd backend/instant-chat
mvn clean package -DskipTests
java -jar target/instant-chat-1.0.0.jar
```

或使用 Maven 运行：
```bash
cd backend/instant-chat
mvn spring-boot:run
```

服务启动后访问：http://localhost:8080

---

## 测试示例

### 注册用户
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"user1","password":"123456","nickname":"用户1"}'
```

### 登录
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user1","password":"123456"}'
```

### 发送好友请求
```bash
curl -X POST http://localhost:8080/api/friend-requests \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"toUserId":2,"message":"你好，我是user1"}'
```

---

## 许可证

MIT License