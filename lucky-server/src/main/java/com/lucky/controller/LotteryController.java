package com.lucky.controller;

import com.lucky.dto.LotteryDTO;
import com.lucky.dto.Result;
import com.lucky.entity.LotteryRound;
import com.lucky.entity.Participant;
import com.lucky.entity.Winner;
import com.lucky.service.LotteryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lottery")
@RequiredArgsConstructor
public class LotteryController {

    private final LotteryService lotteryService;

    @PostMapping("/round")
    public Result<LotteryRound> createRound(@Valid @RequestBody LotteryDTO dto) {
        return Result.ok(lotteryService.createRound(dto));
    }

    @PostMapping("/draw/{roundId}")
    public Result<List<Participant>> draw(@PathVariable Long roundId) {
        return Result.ok(lotteryService.draw(roundId));
    }

    @GetMapping("/rounds")
    public Result<List<LotteryRound>> getRounds(@RequestParam Long activityId) {
        return Result.ok(lotteryService.getRoundsByActivity(activityId));
    }

    @GetMapping("/winners")
    public Result<List<Winner>> getWinners(@RequestParam Long roundId) {
        return Result.ok(lotteryService.getWinnersByRound(roundId));
    }
}
