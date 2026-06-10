package com.lucky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lucky.dto.DanmakuDTO;
import com.lucky.entity.Danmaku;

import java.util.List;

public interface DanmakuService extends IService<Danmaku> {

    /**
     * 发送弹幕
     */
    Danmaku sendDanmaku(DanmakuDTO dto);

    /**
     * 审核弹幕
     * @param id 弹幕ID
     * @param status 1通过 2拒绝
     */
    void reviewDanmaku(Long id, Integer status);

    /**
     * 获取待审核弹幕
     */
    List<Danmaku> getPendingDanmakus(Long activityId);

    /**
     * 获取已通过的弹幕
     */
    List<Danmaku> getApprovedDanmakus(Long activityId);
}
