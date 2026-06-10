package com.lucky.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DanmakuDTO {

    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    private Long participantId;

    @NotBlank(message = "弹幕内容不能为空")
    @Size(max = 200, message = "弹幕内容长度不能超过200个字符")
    private String content;
}
