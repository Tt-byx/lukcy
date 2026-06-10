package com.lucky.config;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Sentinel限流异常处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SentinelBlockExceptionHandler implements BlockExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       BlockException e) throws Exception {
        // 设置响应头
        response.setStatus(429); // Too Many Requests
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // 构建错误响应
        Map<String, Object> result = new HashMap<>();
        result.put("code", 429);
        result.put("data", null);

        if (e instanceof FlowException) {
            result.put("message", "请求过于频繁，请稍后重试");
            log.warn("触发限流: {} - {}", request.getRequestURI(), e.getRule());
        } else {
            result.put("message", "系统繁忙，请稍后重试");
            log.warn("触发限流: {} - {}", request.getRequestURI(), e.getMessage());
        }

        // 写入响应
        response.getWriter().write(objectMapper.writeValueAsString(result));
        response.getWriter().flush();
    }
}
