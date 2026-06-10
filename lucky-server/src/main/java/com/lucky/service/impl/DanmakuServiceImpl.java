package com.lucky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lucky.dto.DanmakuDTO;
import com.lucky.entity.Danmaku;
import com.lucky.entity.Participant;
import com.lucky.mapper.DanmakuMapper;
import com.lucky.mapper.ParticipantMapper;
import com.lucky.service.DanmakuService;
import com.lucky.service.SensitiveWordService;
import com.lucky.exception.BusinessException;
import com.lucky.websocket.LuckyWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DanmakuServiceImpl extends ServiceImpl<DanmakuMapper, Danmaku>
        implements DanmakuService {

    private final SensitiveWordService sensitiveWordService;
    private final LuckyWebSocketHandler webSocketHandler;
    private final ParticipantMapper participantMapper;

    @Override
    public Danmaku sendDanmaku(DanmakuDTO dto) {
        if (dto.getActivityId() == null) {
            throw new BusinessException("活动不存在，请先创建活动");
        }

        // 检查是否被禁言或移出
        if (dto.getParticipantId() != null) {
            Participant sender = participantMapper.selectById(dto.getParticipantId());
            if (sender != null && sender.getIsMuted() != null && sender.getIsMuted() == 1) {
                throw new BusinessException("您已被禁言，无法发送弹幕");
            }
            if (sender != null && sender.getIsBanned() != null && sender.getIsBanned() == 1) {
                throw new BusinessException("您已被移出直播间");
            }
        }
        // 敏感词检查
        if (sensitiveWordService.containsSensitiveWord(dto.getContent())) {
            // 包含敏感词，自动拒绝
            Danmaku danmaku = new Danmaku();
            danmaku.setActivityId(dto.getActivityId());
            danmaku.setParticipantId(dto.getParticipantId());
            danmaku.setContent(dto.getContent());
            danmaku.setStatus(2); // 已拒绝
            save(danmaku);
            return danmaku;
        }

        // 通过过滤，自动审核通过
        Danmaku danmaku = new Danmaku();
        danmaku.setActivityId(dto.getActivityId());
        danmaku.setParticipantId(dto.getParticipantId());
        danmaku.setContent(dto.getContent());
        danmaku.setStatus(1); // 自动通过
        save(danmaku);

        // 广播到大屏（携带发送者名字）
        broadcastDanmaku(danmaku);

        return danmaku;
    }

    @Override
    public void reviewDanmaku(Long id, Integer status) {
        Danmaku danmaku = getById(id);
        if (danmaku != null) {
            danmaku.setStatus(status);
            updateById(danmaku);

            // 如果通过，广播到大屏（携带发送者名字）
            if (status == 1) {
                broadcastDanmaku(danmaku);
            }
        }
    }

    private void broadcastDanmaku(Danmaku danmaku) {
        Participant p = participantMapper.selectById(danmaku.getParticipantId());
        Map<String, Object> data = new HashMap<>();
        data.put("id", danmaku.getId());
        data.put("content", danmaku.getContent());
        data.put("participantName", p != null ? p.getName() : "匿名");
        data.put("activityId", danmaku.getActivityId());
        webSocketHandler.broadcast("danmaku", data);
    }

    @Override
    public List<Danmaku> getPendingDanmakus(Long activityId) {
        return list(new LambdaQueryWrapper<Danmaku>()
                .eq(Danmaku::getActivityId, activityId)
                .eq(Danmaku::getStatus, 0)
                .orderByDesc(Danmaku::getCreatedAt));
    }

    @Override
    public List<Danmaku> getApprovedDanmakus(Long activityId) {
        return list(new LambdaQueryWrapper<Danmaku>()
                .eq(Danmaku::getActivityId, activityId)
                .eq(Danmaku::getStatus, 1)
                .orderByDesc(Danmaku::getCreatedAt));
    }
}
