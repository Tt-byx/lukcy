package com.lucky.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 活动VO - 用于前端展示
 */
@Data
public class ActivityVO {

    private Long id;
    private String name;
    private Integer status;
    private String statusText; // 状态文本：未开始/进行中/已结束
    private LocalDateTime createdAt;
}
