package com.lucky.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置类
 */
@Configuration
public class RabbitConfig {

    /**
     * 弹幕队列
     */
    public static final String DANMAKU_QUEUE = "lucky.danmaku.queue";

    /**
     * 弹幕交换机
     */
    public static final String DANMAKU_EXCHANGE = "lucky.danmaku.exchange";

    /**
     * 弹幕路由键
     */
    public static final String DANMAKU_ROUTING_KEY = "lucky.danmaku.routing";

    /**
     * 抽奖结果队列
     */
    public static final String LOTTERY_RESULT_QUEUE = "lucky.lottery.result.queue";

    /**
     * 抽奖结果交换机
     */
    public static final String LOTTERY_RESULT_EXCHANGE = "lucky.lottery.result.exchange";

    /**
     * 抽奖结果路由键
     */
    public static final String LOTTERY_RESULT_ROUTING_KEY = "lucky.lottery.result.routing";

    /**
     * 在线人数队列
     */
    public static final String ONLINE_COUNT_QUEUE = "lucky.online.count.queue";

    /**
     * 在线人数交换机
     */
    public static final String ONLINE_COUNT_EXCHANGE = "lucky.online.count.exchange";

    /**
     * 在线人数路由键
     */
    public static final String ONLINE_COUNT_ROUTING_KEY = "lucky.online.count.routing";

    // ==================== 弹幕相关 ====================

    @Bean
    public Queue danmakuQueue() {
        return QueueBuilder.durable(DANMAKU_QUEUE).build();
    }

    @Bean
    public DirectExchange danmakuExchange() {
        return new DirectExchange(DANMAKU_EXCHANGE);
    }

    @Bean
    public Binding danmakuBinding(Queue danmakuQueue, DirectExchange danmakuExchange) {
        return BindingBuilder.bind(danmakuQueue).to(danmakuExchange).with(DANMAKU_ROUTING_KEY);
    }

    // ==================== 抽奖结果相关 ====================

    @Bean
    public Queue lotteryResultQueue() {
        return QueueBuilder.durable(LOTTERY_RESULT_QUEUE).build();
    }

    @Bean
    public DirectExchange lotteryResultExchange() {
        return new DirectExchange(LOTTERY_RESULT_EXCHANGE);
    }

    @Bean
    public Binding lotteryResultBinding(Queue lotteryResultQueue, DirectExchange lotteryResultExchange) {
        return BindingBuilder.bind(lotteryResultQueue).to(lotteryResultExchange).with(LOTTERY_RESULT_ROUTING_KEY);
    }

    // ==================== 在线人数相关 ====================

    @Bean
    public Queue onlineCountQueue() {
        return QueueBuilder.durable(ONLINE_COUNT_QUEUE).build();
    }

    @Bean
    public DirectExchange onlineCountExchange() {
        return new DirectExchange(ONLINE_COUNT_EXCHANGE);
    }

    @Bean
    public Binding onlineCountBinding(Queue onlineCountQueue, DirectExchange onlineCountExchange) {
        return BindingBuilder.bind(onlineCountQueue).to(onlineCountExchange).with(ONLINE_COUNT_ROUTING_KEY);
    }

    // ==================== 消息转换器 ====================

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
