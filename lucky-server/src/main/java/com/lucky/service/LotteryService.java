package com.lucky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lucky.dto.LotteryDTO;
import com.lucky.entity.LotteryRound;
import com.lucky.entity.Participant;
import com.lucky.entity.Winner;

import java.util.List;

public interface LotteryService extends IService<LotteryRound> {

    /**
     * 创建抽奖轮次
     */
    LotteryRound createRound(LotteryDTO dto);

    /**
     * 执行抽奖
     * @return 中奖者列表
     */
    List<Participant> draw(Long roundId);

    /**
     * 获取活动的所有轮次
     */
    List<LotteryRound> getRoundsByActivity(Long activityId);

    /**
     * 获取轮次的中奖者
     */
    List<Winner> getWinnersByRound(Long roundId);
}
