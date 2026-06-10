package com.lucky.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("lottery_round")
public class LotteryRound {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long activityId;
    private String roundName;
    private Integer winnerCount;
    private Integer status;
    private LocalDateTime createdAt;

    @Version
    private Integer version;
}
