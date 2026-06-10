package com.lucky.controller;

import com.lucky.dto.ParticipantDTO;
import com.lucky.dto.Result;
import com.lucky.entity.Participant;
import com.lucky.service.ParticipantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/participant")
@RequiredArgsConstructor
public class ParticipantController {

    private final ParticipantService participantService;

    @PostMapping("/register")
    public Result<Participant> register(@Valid @RequestBody ParticipantDTO dto) {
        return Result.ok(participantService.register(dto));
    }

    @GetMapping("/check")
    public Result<Participant> checkRegistered(
            @RequestParam Long activityId,
            @RequestParam String studentId) {
        Participant participant = participantService.checkRegistered(activityId, studentId);
        if (participant != null) {
            return Result.ok(participant);
        }
        return Result.error("未找到注册记录");
    }

    @GetMapping("/list")
    public Result<List<Participant>> list(@RequestParam Long activityId) {
        return Result.ok(participantService.getByActivity(activityId));
    }

    @GetMapping("/count")
    public Result<Integer> count(@RequestParam Long activityId) {
        return Result.ok(participantService.getCountByActivity(activityId));
    }
}
