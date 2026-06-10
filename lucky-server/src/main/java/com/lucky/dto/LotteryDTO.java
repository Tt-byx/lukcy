package com.lucky.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LotteryDTO {

    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    @NotBlank(message = "轮次名称不能为空")
    @Size(max = 100, message = "轮次名称长度不能超过100个字符")
    private String roundName;

    @NotNull(message = "中奖人数不能为空")
    @Min(value = 1, message = "中奖人数至少为1")
    private Integer winnerCount;
}
