package com.lucky.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("participant")
public class Participant {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long activityId;
    private String name;
    private String phone;
    private String studentId;
    private Integer status;
    private Integer isMuted;
    private Integer isBanned;
    private LocalDateTime createdAt;
}
