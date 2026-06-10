-- 抽奖程序数据库初始化脚本

CREATE DATABASE IF NOT EXISTS lucky DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE lucky;

-- 活动表
CREATE TABLE IF NOT EXISTS activity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '活动名称',
    status TINYINT DEFAULT 0 COMMENT '0未开始 1进行中 2已结束',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动表';

-- 参与者表
CREATE TABLE IF NOT EXISTS participant (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    activity_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    phone VARCHAR(20) COMMENT '手机号',
    student_id VARCHAR(30) COMMENT '学号',
    status TINYINT DEFAULT 1 COMMENT '1已参与 0已中奖 3已移除抽奖',
    is_muted TINYINT DEFAULT 0 COMMENT '是否禁言 0否 1是',
    is_banned TINYINT DEFAULT 0 COMMENT '是否被移除直播间 0否 1是',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_activity (activity_id),
    INDEX idx_student (activity_id, student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='参与者表';

-- 抽奖轮次表
CREATE TABLE IF NOT EXISTS lottery_round (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    activity_id BIGINT NOT NULL,
    round_name VARCHAR(100) COMMENT '轮次名称，如：一等奖、二等奖',
    winner_count INT DEFAULT 1 COMMENT '本轮抽取人数',
    status TINYINT DEFAULT 0 COMMENT '0未开始 1已完成',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_activity (activity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽奖轮次表';

-- 中奖记录表
CREATE TABLE IF NOT EXISTS winner (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    round_id BIGINT NOT NULL,
    participant_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_round (round_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='中奖记录表';

-- 弹幕表
CREATE TABLE IF NOT EXISTS danmaku (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    activity_id BIGINT NOT NULL,
    participant_id BIGINT COMMENT '发送者',
    content VARCHAR(200) NOT NULL COMMENT '弹幕内容',
    status TINYINT DEFAULT 0 COMMENT '0待审核 1已通过 2已拒绝',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_activity_status (activity_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='弹幕表';

-- 敏感词表
CREATE TABLE IF NOT EXISTS sensitive_word (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    word VARCHAR(50) NOT NULL COMMENT '敏感词',
    UNIQUE INDEX uk_word (word)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='敏感词表';

-- 插入一些默认敏感词
INSERT IGNORE INTO sensitive_word (word) VALUES
('傻逼'), ('操你'), ('狗日'), ('混蛋'), ('王八蛋'), ('去死'), ('垃圾');

-- 屏幕配置表
CREATE TABLE IF NOT EXISTS screen_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    background_type VARCHAR(20) COMMENT '背景类型: image/video',
    background_url VARCHAR(500) COMMENT '背景文件URL',
    danmaku_area VARCHAR(20) DEFAULT 'full' COMMENT '弹幕显示区域: full/top/bottom',
    danmaku_opacity INT DEFAULT 80 COMMENT '弹幕不透明度(0-100)',
    danmaku_font_size INT DEFAULT 28 COMMENT '弹幕字号',
    danmaku_speed INT DEFAULT 10 COMMENT '弹幕滚动速度(秒)',
    mobile_background_type VARCHAR(20) COMMENT '手机端背景类型',
    mobile_background_url VARCHAR(500) COMMENT '手机端背景URL',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='屏幕配置表';

-- 插入默认配置
INSERT IGNORE INTO screen_config (id, background_type, background_url, danmaku_area, danmaku_opacity, danmaku_font_size, danmaku_speed) VALUES (1, NULL, NULL, 'full', 80, 28, 10);

-- 管理员用户表
CREATE TABLE IF NOT EXISTS admin_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
    role VARCHAR(20) DEFAULT 'ADMIN' COMMENT '角色：ADMIN/SUPER_ADMIN',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员用户表';
