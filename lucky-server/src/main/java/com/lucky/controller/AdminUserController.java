package com.lucky.controller;

import com.lucky.dto.Result;
import com.lucky.entity.Participant;
import com.lucky.service.ParticipantService;
import com.lucky.websocket.LuckyWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
public class AdminUserController {

    private final ParticipantService participantService;
    private final LuckyWebSocketHandler webSocketHandler;

    /**
     * 获取指定活动的所有参与者列表
     */
    @GetMapping("/list")
    public Result<List<Participant>> list(@RequestParam Long activityId) {
        return Result.ok(participantService.getByActivity(activityId));
    }

    /**
     * 获取当前在线用户列表
     */
    @GetMapping("/online")
    public Result<List<Participant>> onlineUsers(@RequestParam Long activityId) {
        Set<Long> onlineIds = webSocketHandler.getOnlineParticipantIds();
        if (onlineIds.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }
        List<Participant> all = participantService.getByActivity(activityId);
        List<Participant> online = all.stream()
                .filter(p -> onlineIds.contains(p.getId()))
                .collect(Collectors.toList());
        return Result.ok(online);
    }

    /**
     * 禁言/取消禁言
     */
    @PutMapping("/{id}/mute")
    public Result<Void> toggleMute(@PathVariable Long id, @RequestParam boolean muted) {
        Participant p = participantService.getById(id);
        if (p == null) return Result.error("用户不存在");

        p.setIsMuted(muted ? 1 : 0);
        participantService.updateById(p);

        if (muted) {
            webSocketHandler.sendToUser(id, "muted", "您已被管理员禁言");
        }
        return Result.ok();
    }

    /**
     * 移除/恢复直播间
     */
    @PutMapping("/{id}/ban")
    public Result<Void> toggleBan(@PathVariable Long id, @RequestParam boolean banned) {
        Participant p = participantService.getById(id);
        if (p == null) return Result.error("用户不存在");

        p.setIsBanned(banned ? 1 : 0);
        participantService.updateById(p);

        if (banned) {
            webSocketHandler.sendToUser(id, "banned", "您已被移出直播间");
            webSocketHandler.disconnectUser(id);
        }
        return Result.ok();
    }

    /**
     * 移除/恢复抽奖资格
     */
    @PutMapping("/{id}/lottery")
    public Result<Void> toggleLottery(@PathVariable Long id, @RequestParam boolean removed) {
        Participant p = participantService.getById(id);
        if (p == null) return Result.error("用户不存在");

        p.setStatus(removed ? 3 : 1); // 3=已移除抽奖, 1=正常参与
        participantService.updateById(p);

        if (removed) {
            webSocketHandler.sendToUser(id, "lottery_removed", "您已被移除抽奖资格");
        }
        return Result.ok();
    }
}
