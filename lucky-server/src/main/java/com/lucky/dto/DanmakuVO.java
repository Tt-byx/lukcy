package com.lucky.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 弹幕VO - 用于前端展示
 */
@Data
public class DanmakuVO {

    private Long id;
    private Long activityId;
    private Long participantId;
    private String participantName; // 发送者姓名
    private String content;
    private Integer status;
    private String statusText; // 状态文本：待审核/已通过/已拒绝
    private LocalDateTime createdAt;
}
