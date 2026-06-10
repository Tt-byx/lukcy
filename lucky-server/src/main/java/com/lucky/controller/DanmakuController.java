package com.lucky.controller;

import com.lucky.dto.DanmakuDTO;
import com.lucky.dto.Result;
import com.lucky.entity.Danmaku;
import com.lucky.service.DanmakuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/danmaku")
@RequiredArgsConstructor
public class DanmakuController {

    private final DanmakuService danmakuService;

    @PostMapping("/send")
    public Result<Danmaku> send(@Valid @RequestBody DanmakuDTO dto) {
        Danmaku danmaku = danmakuService.sendDanmaku(dto);
        if (danmaku.getStatus() == 2) {
            return Result.error(403, "弹幕包含敏感词，已被拒绝");
        }
        return Result.ok(danmaku);
    }

    @PutMapping("/{id}/review")
    public Result<Void> review(@PathVariable Long id, @RequestParam Integer status) {
        danmakuService.reviewDanmaku(id, status);
        return Result.ok();
    }

    @GetMapping("/pending")
    public Result<List<Danmaku>> pending(@RequestParam Long activityId) {
        return Result.ok(danmakuService.getPendingDanmakus(activityId));
    }

    @GetMapping("/approved")
    public Result<List<Danmaku>> approved(@RequestParam Long activityId) {
        return Result.ok(danmakuService.getApprovedDanmakus(activityId));
    }
}
