package com.lucky.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 参与者VO - 用于前端展示
 */
@Data
public class ParticipantVO {

    private Long id;
    private Long activityId;
    private String name;
    private String studentId;
    private Integer status;
    private String statusText; // 状态文本：已参与/已中奖/已移除
    private Integer isMuted;
    private Integer isBanned;
    private LocalDateTime createdAt;
}
