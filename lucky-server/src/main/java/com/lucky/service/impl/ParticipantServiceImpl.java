package com.lucky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lucky.dto.ParticipantDTO;
import com.lucky.entity.Participant;
import com.lucky.mapper.ParticipantMapper;
import com.lucky.service.ParticipantService;
import com.lucky.exception.BusinessException;
import com.lucky.websocket.LuckyWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParticipantServiceImpl extends ServiceImpl<ParticipantMapper, Participant>
        implements ParticipantService {

    private final LuckyWebSocketHandler webSocketHandler;

    @Override
    public Participant register(ParticipantDTO dto) {
        if (dto.getActivityId() == null) {
            throw new BusinessException("活动不存在，请先创建活动");
        }
        // 检查是否已经参与过
        long count = count(new LambdaQueryWrapper<Participant>()
                .eq(Participant::getActivityId, dto.getActivityId())
                .eq(Participant::getStudentId, dto.getStudentId()));

        if (count > 0) {
            throw new BusinessException("您已经参与过本次活动");
        }

        Participant participant = new Participant();
        participant.setActivityId(dto.getActivityId());
        participant.setName(dto.getName());
        participant.setPhone(dto.getPhone());
        participant.setStudentId(dto.getStudentId());
        participant.setStatus(1);
        save(participant);

        // 广播参与者数量更新
        int total = getCountByActivity(dto.getActivityId());
        webSocketHandler.broadcast("participant_update", total);

        return participant;
    }

    @Override
    public Participant checkRegistered(Long activityId, String studentId) {
        Participant p = getOne(new LambdaQueryWrapper<Participant>()
                .eq(Participant::getActivityId, activityId)
                .eq(Participant::getStudentId, studentId)
                .last("LIMIT 1"));
        if (p != null && p.getIsBanned() != null && p.getIsBanned() == 1) {
            throw new BusinessException("您已被移出直播间，无法进入");
        }
        return p;
    }

    @Override
    public List<Participant> getByActivity(Long activityId) {
        return list(new LambdaQueryWrapper<Participant>()
                .eq(Participant::getActivityId, activityId)
                .orderByDesc(Participant::getCreatedAt));
    }

    @Override
    public int getCountByActivity(Long activityId) {
        return (int) count(new LambdaQueryWrapper<Participant>()
                .eq(Participant::getActivityId, activityId));
    }
}
