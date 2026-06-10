package com.lucky.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("winner")
public class Winner {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long roundId;

    private Long participantId;

    private LocalDateTime createdAt;
}
