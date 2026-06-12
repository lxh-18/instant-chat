-- 创建数据库
CREATE DATABASE IF NOT EXISTS instant_chat DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE instant_chat;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
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

-- 好友分组表
CREATE TABLE IF NOT EXISTS friend_groups (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    group_name VARCHAR(100),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 好友关系表
CREATE TABLE IF NOT EXISTS friends (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    friend_id BIGINT NOT NULL,
    group_id BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_friend_id (friend_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 好友请求表
CREATE TABLE IF NOT EXISTS friend_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    from_user_id BIGINT NOT NULL,
    to_user_id BIGINT NOT NULL,
    message VARCHAR(255),
    status TINYINT DEFAULT 0 COMMENT '0:待处理 1:已同意 2:已拒绝 3:已过期',
    is_read TINYINT DEFAULT 0 COMMENT '0:未读 1:已读',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    expires_at DATETIME,
    INDEX idx_to_user_id (to_user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 群邀请表
CREATE TABLE IF NOT EXISTS group_invitations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_id BIGINT NOT NULL,
    inviter_user_id BIGINT NOT NULL,
    invitee_user_id BIGINT NOT NULL,
    message VARCHAR(255),
    status TINYINT DEFAULT 0 COMMENT '0:待处理 1:已同意 2:已拒绝',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_group_id (group_id),
    INDEX idx_invitee_user_id (invitee_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 群组表
CREATE TABLE IF NOT EXISTS chat_groups (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_name VARCHAR(100),
    group_avatar VARCHAR(500),
    owner_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 群成员表
CREATE TABLE IF NOT EXISTS group_members (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    joined_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_group_id (group_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 私聊消息表
CREATE TABLE IF NOT EXISTS private_messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    from_user_id BIGINT NOT NULL,
    to_user_id BIGINT NOT NULL,
    content TEXT,
    message_type TINYINT DEFAULT 1 COMMENT '1:文本 2:图片 3:语音',
    file_path VARCHAR(500),
    is_read TINYINT DEFAULT 0 COMMENT '0:未读 1:已读',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_from_user_id (from_user_id),
    INDEX idx_to_user_id (to_user_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 群聊消息表
CREATE TABLE IF NOT EXISTS group_messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_id BIGINT NOT NULL,
    from_user_id BIGINT NOT NULL,
    content TEXT,
    message_type TINYINT DEFAULT 1 COMMENT '1:文本 2:图片 3:语音',
    file_path VARCHAR(500),
    is_read TINYINT DEFAULT 0 COMMENT '0:未读 1:已读',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_group_id (group_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;