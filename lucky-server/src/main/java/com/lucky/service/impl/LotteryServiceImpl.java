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
import com.lucky.mq.MessageProducer;
import com.lucky.service.LotteryService;
import com.lucky.exception.BusinessException;
import com.lucky.websocket.LuckyWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class LotteryServiceImpl extends ServiceImpl<LotteryRoundMapper, LotteryRound>
        implements LotteryService {

    private final ParticipantMapper participantMapper;
    private final WinnerMapper winnerMapper;
    private final LuckyWebSocketHandler webSocketHandler;
    private final MessageProducer messageProducer;
    private final RedissonClient redissonClient;

    /**
     * 分布式锁前缀
     */
    private static final String LOTTERY_LOCK_PREFIX = "lucky:lottery:lock:";

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
    public List<Participant> draw(Long roundId) {
        // 获取分布式锁，防止并发抽奖
        String lockKey = LOTTERY_LOCK_PREFIX + roundId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 尝试获取锁，等待3秒，锁持有时间30秒
            if (!lock.tryLock(3, 30, TimeUnit.SECONDS)) {
                throw new BusinessException("抽奖正在进行中，请稍后重试");
            }

            log.info("获取分布式锁成功，开始抽奖: roundId={}", roundId);

            // 获取锁成功，执行抽奖逻辑
            return doDraw(roundId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("抽奖被中断，请重试");
        } finally {
            // 释放锁（只释放自己持有的锁）
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("释放分布式锁: roundId={}", roundId);
            }
        }
    }

    /**
     * 执行抽奖逻辑（在分布式锁保护下）
     */
    @Transactional
    protected List<Participant> doDraw(Long roundId) {
        LotteryRound round = getById(roundId);
        if (round == null) {
            throw new BusinessException("轮次不存在");
        }

        // 检查轮次状态
        if (round.getStatus() == 1) {
            throw new BusinessException("该轮次已完成抽奖");
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

        // 通知大屏开始抽奖动画
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

        // 更新轮次状态
        round.setStatus(1);
        updateById(round);

        // 通过消息队列异步广播抽奖结果
        messageProducer.sendLotteryResult(winners);

        log.info("抽奖完成: roundId={}, 中奖人数={}", roundId, winners.size());

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
