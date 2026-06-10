package com.lucky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lucky.dto.LotteryDTO;
import com.lucky.entity.LotteryRound;
import com.lucky.entity.Participant;
import com.lucky.entity.Winner;
import com.lucky.mapper.LotteryRoundMapper;
import com.lucky.mapper.ParticipantMapper;
import com.lucky.mapper.WinnerMapper;
import com.lucky.service.LotteryService;
import com.lucky.exception.BusinessException;
import com.lucky.websocket.LuckyWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LotteryServiceImpl extends ServiceImpl<LotteryRoundMapper, LotteryRound>
        implements LotteryService {

    private final ParticipantMapper participantMapper;
    private final WinnerMapper winnerMapper;
    private final LuckyWebSocketHandler webSocketHandler;

    @Override
    public LotteryRound createRound(LotteryDTO dto) {
        if (dto.getActivityId() == null) {
            throw new BusinessException("活动不存在，请先创建活动");
        }
        LotteryRound round = new LotteryRound();
        round.setActivityId(dto.getActivityId());
        round.setRoundName(dto.getRoundName());
        round.setWinnerCount(dto.getWinnerCount());
        round.setStatus(0);
        save(round);
        return round;
    }

    @Override
    @Transactional
    public List<Participant> draw(Long roundId) {
        LotteryRound round = getById(roundId);
        if (round == null) {
            throw new BusinessException("轮次不存在");
        }

        // 获取该活动下所有未中奖的参与者
        List<Participant> candidates = participantMapper.selectList(
                new LambdaQueryWrapper<Participant>()
                        .eq(Participant::getActivityId, round.getActivityId())
                        .eq(Participant::getStatus, 1)
        );

        if (candidates.isEmpty()) {
            throw new BusinessException("没有可抽奖的参与者");
        }

        // 通知大屏开始抽奖动画（参与者检查通过后再广播）
        webSocketHandler.broadcast("lottery_start", roundId);

        // 随机打乱
        Collections.shuffle(candidates);

        // 取出中奖者（不超过候选人数）
        int count = Math.min(round.getWinnerCount(), candidates.size());
        List<Participant> winners = new ArrayList<>(candidates.subList(0, count));

        // 保存中奖记录，更新参与者状态
        for (Participant winner : winners) {
            Winner record = new Winner();
            record.setRoundId(roundId);
            record.setParticipantId(winner.getId());
            winnerMapper.insert(record);

            winner.setStatus(0); // 标记已中奖
            participantMapper.updateById(winner);
        }

        // 更新轮次状态（乐观锁）
        round.setStatus(1);
        boolean updated = updateById(round);
        if (!updated) {
            throw new BusinessException("抽奖正在进行中，请稍后重试");
        }

        // 广播抽奖结果
        webSocketHandler.broadcast("lottery_result", winners);

        return winners;
    }

    @Override
    public List<LotteryRound> getRoundsByActivity(Long activityId) {
        return list(new LambdaQueryWrapper<LotteryRound>()
                .eq(LotteryRound::getActivityId, activityId)
                .orderByAsc(LotteryRound::getCreatedAt));
    }

    @Override
    public List<Winner> getWinnersByRound(Long roundId) {
        return winnerMapper.selectList(
                new LambdaQueryWrapper<Winner>()
                        .eq(Winner::getRoundId, roundId)
        );
    }
}
