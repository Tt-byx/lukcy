package com.lucky.controller;

import com.lucky.dto.Result;
import com.lucky.websocket.LuckyWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/ws")
@RequiredArgsConstructor
public class WsController {

    private final LuckyWebSocketHandler webSocketHandler;

    @GetMapping("/online")
    public Result<Map<String, Integer>> getOnlineCount() {
        return Result.ok(Map.of("count", webSocketHandler.getOnlineCount()));
    }
}
