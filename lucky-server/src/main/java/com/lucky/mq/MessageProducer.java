package com.lucky.mq;

import com.lucky.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 消息生产者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送弹幕消息
     */
    public void sendDanmaku(Map<String, Object> danmakuData) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitConfig.DANMAKU_EXCHANGE,
                    RabbitConfig.DANMAKU_ROUTING_KEY,
                    danmakuData
            );
            log.debug("弹幕消息已发送到队列: {}", danmakuData.get("content"));
        } catch (Exception e) {
            log.error("发送弹幕消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 发送抽奖结果消息
     */
    public void sendLotteryResult(Object winners) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitConfig.LOTTERY_RESULT_EXCHANGE,
                    RabbitConfig.LOTTERY_RESULT_ROUTING_KEY,
                    winners
            );
            log.debug("抽奖结果消息已发送到队列");
        } catch (Exception e) {
            log.error("发送抽奖结果消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 发送在线人数更新消息
     */
    public void sendOnlineCount(int count) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitConfig.ONLINE_COUNT_EXCHANGE,
                    RabbitConfig.ONLINE_COUNT_ROUTING_KEY,
                    count
            );
            log.debug("在线人数消息已发送到队列: {}", count);
        } catch (Exception e) {
            log.error("发送在线人数消息失败: {}", e.getMessage(), e);
        }
    }
}
