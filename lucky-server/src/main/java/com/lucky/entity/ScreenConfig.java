package com.lucky.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("screen_config")
public class ScreenConfig {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String backgroundType;
    private String backgroundUrl;
    
    // 弹幕设置
    private String danmakuArea;
    private Integer danmakuOpacity;
    private Integer danmakuFontSize;
    private Integer danmakuSpeed;

    // 手机端背景
    private String mobileBackgroundType;
    private String mobileBackgroundUrl;

    private LocalDateTime updatedAt;
}
