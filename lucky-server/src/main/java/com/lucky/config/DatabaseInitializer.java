package com.lucky.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements ApplicationRunner {

    private final DataSource dataSource;

    @Override
    public void run(ApplicationArguments args) {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS screen_config (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                    "background_type VARCHAR(20) COMMENT '背景类型: image/video', " +
                    "background_url VARCHAR(500) COMMENT '背景文件URL', " +
                    "danmaku_area VARCHAR(20) DEFAULT 'full' COMMENT '弹幕显示区域: full/top/bottom', " +
                    "danmaku_opacity INT DEFAULT 80 COMMENT '弹幕不透明度(0-100)', " +
                    "danmaku_font_size INT DEFAULT 28 COMMENT '弹幕字号', " +
                    "danmaku_speed INT DEFAULT 10 COMMENT '弹幕滚动速度(秒)', " +
                    "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='屏幕配置表'");
            stmt.execute("INSERT IGNORE INTO screen_config (id, background_type, background_url, danmaku_area, danmaku_opacity, danmaku_font_size, danmaku_speed) VALUES (1, NULL, NULL, 'full', 80, 28, 10)");
            // 添加手机端背景列（如果不存在）
            try {
                stmt.execute("ALTER TABLE screen_config ADD COLUMN mobile_background_type VARCHAR(20) COMMENT '手机端背景类型'");
            } catch (Exception e) { log.info("mobile_background_type 列已存在"); }
            try {
                stmt.execute("ALTER TABLE screen_config ADD COLUMN mobile_background_url VARCHAR(500) COMMENT '手机端背景URL'");
            } catch (Exception e) { log.info("mobile_background_url 列已存在"); }
            // 添加 participant 表新字段
            try {
                stmt.execute("ALTER TABLE participant ADD COLUMN is_muted TINYINT DEFAULT 0 COMMENT '是否禁言'");
            } catch (Exception e) { log.info("is_muted 列已存在"); }
            try {
                stmt.execute("ALTER TABLE participant ADD COLUMN is_banned TINYINT DEFAULT 0 COMMENT '是否被移除直播间'");
            } catch (Exception e) { log.info("is_banned 列已存在"); }
            // 添加 lottery_round 表 version 字段（乐观锁）
            try {
                stmt.execute("ALTER TABLE lottery_round ADD COLUMN version INT DEFAULT 0 COMMENT '版本号（乐观锁）'");
            } catch (Exception e) { log.info("version 列已存在"); }
            log.info("数据库表初始化完成");
        } catch (Exception e) {
            log.error("screen_config 表初始化失败: {}", e.getMessage());
        }
    }
}