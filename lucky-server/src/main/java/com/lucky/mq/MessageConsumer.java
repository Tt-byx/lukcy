package com.lucky.mq;

import com.lucky.config.RabbitConfig;
import com.lucky.websocket.LuckyWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 消息消费者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageConsumer {

    private final LuckyWebSocketHandler webSocketHandler;

    /**
     * 消费弹幕消息，广播到所有WebSocket客户端
     */
    @RabbitListener(queues = RabbitConfig.DANMAKU_QUEUE)
    public void handleDanmaku(Map<String, Object> danmakuData) {
        try {
            webSocketHandler.broadcast("danmaku", danmakuData);
            log.debug("弹幕消息已广播: {}", danmakuData.get("content"));
        } catch (Exception e) {
            log.error("处理弹幕消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 消费抽奖结果消息，广播到所有WebSocket客户端
     */
    @RabbitListener(queues = RabbitConfig.LOTTERY_RESULT_QUEUE)
    public void handleLotteryResult(Object winners) {
        try {
            webSocketHandler.broadcast("lottery_result", winners);
            log.debug("抽奖结果已广播");
        } catch (Exception e) {
            log.error("处理抽奖结果消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 消费在线人数消息，广播到所有WebSocket客户端
     */
    @RabbitListener(queues = RabbitConfig.ONLINE_COUNT_QUEUE)
    public void handleOnlineCount(Integer count) {
        try {
            webSocketHandler.broadcast("online_update", count);
            log.debug("在线人数已广播: {}", count);
        } catch (Exception e) {
            log.error("处理在线人数消息失败: {}", e.getMessage(), e);
        }
    }
}
