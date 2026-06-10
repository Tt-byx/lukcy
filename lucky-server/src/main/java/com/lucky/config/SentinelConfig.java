package com.lucky.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Sentinel限流配置
 */
@Slf4j
@Configuration
public class SentinelConfig {

    @PostConstruct
    public void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();

        // 1. 弹幕发送接口限流 - 每秒最多100次
        FlowRule danmakuRule = new FlowRule();
        danmakuRule.setResource("POST:/api/danmaku/send");
        danmakuRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        danmakuRule.setCount(100);
        danmakuRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_WARM_UP);
        danmakuRule.setWarmUpPeriodSec(10); // 10秒预热
        rules.add(danmakuRule);

        // 2. 参与者注册接口限流 - 每秒最多50次
        FlowRule registerRule = new FlowRule();
        registerRule.setResource("POST:/api/participant/register");
        registerRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        registerRule.setCount(50);
        registerRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_WARM_UP);
        registerRule.setWarmUpPeriodSec(10);
        rules.add(registerRule);

        // 3. 登录接口限流 - 每秒最多10次（防暴力破解）
        FlowRule loginRule = new FlowRule();
        loginRule.setResource("POST:/api/auth/login");
        loginRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        loginRule.setCount(10);
        loginRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_WARM_UP);
        loginRule.setWarmUpPeriodSec(5);
        rules.add(loginRule);

        // 4. 抽奖接口限流 - 每秒最多5次
        FlowRule drawRule = new FlowRule();
        drawRule.setResource("POST:/api/lottery/draw/{roundId}");
        drawRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        drawRule.setCount(5);
        drawRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_WARM_UP);
        drawRule.setWarmUpPeriodSec(5);
        rules.add(drawRule);

        // 5. 全局默认限流 - 每秒最多500次
        FlowRule globalRule = new FlowRule();
        globalRule.setResource("global");
        globalRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        globalRule.setCount(500);
        globalRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_WARM_UP);
        globalRule.setWarmUpPeriodSec(30);
        rules.add(globalRule);

        FlowRuleManager.loadRules(rules);
        log.info("Sentinel限流规则加载完成，共{}条规则", rules.size());
    }
}
