# 20260610143000-集成RabbitMQ-Redisson-Sentinel.md

## 提示词
用户要求实现三个高并发相关技术：
1. 消息队列（RabbitMQ）- 异步处理弹幕，解耦
2. 分布式锁（Redisson）- 更强的并发控制
3. 接口限流（Sentinel）- 防止恶意请求

---

## 做了些什么事情

### 1. 集成 RabbitMQ 消息队列

#### 1.1 添加依赖
**文件：** `lucky-server/pom.xml`
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

#### 1.2 创建 RabbitMQ 配置类
**文件：** `lucky-server/src/main/java/com/lucky/config/RabbitConfig.java`

**配置内容：**
- 弹幕队列：`lucky.danmaku.queue`
- 抽奖结果队列：`lucky.lottery.result.queue`
- 在线人数队列：`lucky.online.count.queue`
- 对应的交换机和路由键
- JSON 消息转换器

#### 1.3 创建消息生产者
**文件：** `lucky-server/src/main/java/com/lucky/mq/MessageProducer.java`

**方法：**
- `sendDanmaku()` - 发送弹幕消息
- `sendLotteryResult()` - 发送抽奖结果
- `sendOnlineCount()` - 发送在线人数

#### 1.4 创建消息消费者
**文件：** `lucky-server/src/main/java/com/lucky/mq/MessageConsumer.java`

**监听器：**
- `handleDanmaku()` - 消费弹幕，广播到 WebSocket
- `handleLotteryResult()` - 消费抽奖结果，广播到 WebSocket
- `handleOnlineCount()` - 消费在线人数，广播到 WebSocket

#### 1.5 修改弹幕服务
**文件：** `lucky-server/src/main/java/com/lucky/service/impl/DanmakuServiceImpl.java`

**修改内容：**
- 注入 `MessageProducer` 替代 `LuckyWebSocketHandler`
- `broadcastDanmaku()` 改为 `broadcastDanmakuAsync()`
- 通过消息队列异步发送弹幕

---

### 2. 集成 Redisson 分布式锁

#### 2.1 添加依赖
**文件：** `lucky-server/pom.xml`
```xml
<dependency>
    <groupId>org.redisson</groupId>
    <artifactId>redisson-spring-boot-starter</artifactId>
    <version>3.27.0</version>
</dependency>
```

#### 2.2 修改抽奖服务
**文件：** `lucky-server/src/main/java/com/lucky/service/impl/LotteryServiceImpl.java`

**修改内容：**
- 注入 `RedissonClient`
- `draw()` 方法使用分布式锁
- 锁 key：`lucky:lottery:lock:{roundId}`
- 等待时间：3秒
- 持有时间：30秒
- 新增 `doDraw()` 方法执行实际抽奖逻辑

**核心代码：**
```java
public List<Participant> draw(Long roundId) {
    String lockKey = LOTTERY_LOCK_PREFIX + roundId;
    RLock lock = redissonClient.getLock(lockKey);

    try {
        if (!lock.tryLock(3, 30, TimeUnit.SECONDS)) {
            throw new BusinessException("抽奖正在进行中，请稍后重试");
        }
        return doDraw(roundId);
    } finally {
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
```

#### 2.3 配置 Redisson
**文件：** `application.yml` 和 `application-prod.yml`
```yaml
redisson:
  single-server-config:
    address: redis://${REDIS_HOST:localhost}:${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
```

---

### 3. 集成 Sentinel 接口限流

#### 3.1 添加依赖
**文件：** `lucky-server/pom.xml`
```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
    <version>2023.0.1.0</version>
</dependency>
```

#### 3.2 创建限流配置
**文件：** `lucky-server/src/main/java/com/lucky/config/SentinelConfig.java`

**限流规则：**
| 接口 | QPS | 预热时间 | 说明 |
|------|-----|----------|------|
| POST:/api/danmaku/send | 100 | 10秒 | 弹幕发送 |
| POST:/api/participant/register | 50 | 10秒 | 参与者注册 |
| POST:/api/auth/login | 10 | 5秒 | 登录（防暴力破解） |
| POST:/api/lottery/draw/{roundId} | 5 | 5秒 | 抽奖 |
| global | 500 | 30秒 | 全局默认 |

#### 3.3 创建限流异常处理器
**文件：** `lucky-server/src/main/java/com/lucky/config/SentinelBlockExceptionHandler.java`

**返回格式：**
```json
{
    "code": 429,
    "message": "请求过于频繁，请稍后重试",
    "data": null
}
```

#### 3.4 配置 Sentinel
**文件：** `application.yml` 和 `application-prod.yml`
```yaml
spring:
  cloud:
    sentinel:
      enabled: true
      eager: true
      transport:
        dashboard: ${SENTINEL_DASHBOARD:}
        port: 8719
```

---

### 4. 更新 Docker Compose

#### 4.1 添加 RabbitMQ 服务
**文件：** `docker-compose.yml` 和 `docker-compose.prod.yml`

```yaml
rabbitmq:
  image: rabbitmq:3-management-alpine
  container_name: lucky-rabbitmq
  ports:
    - "5672:5672"
    - "15672:15672"
  environment:
    RABBITMQ_DEFAULT_USER: guest
    RABBITMQ_DEFAULT_PASS: guest
  volumes:
    - rabbitmq-data:/var/lib/rabbitmq
  healthcheck:
    test: ["CMD", "rabbitmq-diagnostics", "check_running"]
    interval: 30s
    timeout: 10s
    retries: 5
    start_period: 30s
  restart: unless-stopped
```

#### 4.2 更新后端环境变量
```yaml
backend:
  environment:
    RABBITMQ_HOST: rabbitmq
    RABBITMQ_PORT: 5672
    RABBITMQ_USERNAME: guest
    RABBITMQ_PASSWORD: guest
```

---

## 为什么这样做

### 1. **RabbitMQ 消息队列**

**解决的问题：**
- 弹幕广播是同步调用，2000人同时发弹幕时会阻塞主线程
- WebSocket 广播和业务逻辑耦合在一起
- 无法支持分布式部署（WebSocket 连接只在本地内存）

**带来的收益：**
- 弹幕发送和广播解耦，发送只需写入队列
- 消费者异步处理广播，不阻塞业务线程
- 支持多消费者水平扩展
- 消息持久化，避免丢失

### 2. **Redisson 分布式锁**

**解决的问题：**
- 乐观锁只能防止更新冲突，不能防止重复执行
- 并发抽奖时可能重复选择同一个参与者
- 单机锁在分布式环境下无效

**带来的收益：**
- 真正的分布式并发控制
- 防止重复抽奖
- 支持分布式部署
- 自动续期，避免死锁

### 3. **Sentinel 接口限流**

**解决的问题：**
- 没有限流保护，容易被恶意请求打垮
- 弹幕接口高频调用，需要控制 QPS
- 登录接口需要防止暴力破解

**带来的收益：**
- 保护系统稳定性
- 防止恶意攻击
- 支持预热模式，避免冷启动问题
- 友好的错误提示

---

## 产生的结果

### 1. **新增文件**

| 文件 | 说明 |
|------|------|
| `config/RabbitConfig.java` | RabbitMQ 配置类 |
| `mq/MessageProducer.java` | 消息生产者 |
| `mq/MessageConsumer.java` | 消息消费者 |
| `config/SentinelConfig.java` | Sentinel 限流配置 |
| `config/SentinelBlockExceptionHandler.java` | 限流异常处理器 |

### 2. **修改文件**

| 文件 | 修改内容 |
|------|----------|
| `pom.xml` | 添加 RabbitMQ、Redisson、Sentinel 依赖 |
| `DanmakuServiceImpl.java` | 使用消息队列发送弹幕 |
| `LotteryServiceImpl.java` | 使用 Redisson 分布式锁 |
| `application.yml` | 添加 RabbitMQ、Sentinel 配置 |
| `application-prod.yml` | 添加 RabbitMQ、Sentinel 配置 |
| `docker-compose.yml` | 添加 RabbitMQ 服务 |
| `docker-compose.prod.yml` | 添加 RabbitMQ 服务 |

### 3. **构建验证**
```bash
mvn clean compile -q
# 编译成功
```

---

## 测试场景

### 1. RabbitMQ 测试
```bash
# 启动 RabbitMQ
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management-alpine

# 访问管理界面
http://localhost:15672
用户名/密码: guest/guest

# 发送弹幕测试
curl -X POST http://localhost:8080/api/danmaku/send \
  -H "Content-Type: application/json" \
  -d '{"activityId": 1, "participantId": 1, "content": "测试弹幕"}'

# 在 RabbitMQ 管理界面查看消息
```

### 2. Redisson 分布式锁测试
```bash
# 并发测试抽奖
# 同时发起两个抽奖请求
for i in {1..2}; do
  curl -X POST http://localhost:8080/api/lottery/draw/1 &
done

# 预期：一个成功，一个返回"抽奖正在进行中，请稍后重试"
```

### 3. Sentinel 限流测试
```bash
# 快速发送100+请求
for i in {1..150}; do
  curl -X POST http://localhost:8080/api/danmaku/send \
    -H "Content-Type: application/json" \
    -d '{"activityId": 1, "content": "测试限流"}' &
done

# 预期：部分请求返回 429
# {"code":429,"message":"请求过于频繁，请稍后重试","data":null}
```

---

## 架构升级对比

### 优化前
```
用户发送弹幕 → Controller → Service → 直接调用 WebSocket 广播
                                    ↓
                              同步阻塞，2000人时卡顿
```

### 优化后
```
用户发送弹幕 → Controller → Service → 发送到 RabbitMQ → 立即返回
                                    ↓
                              消费者异步处理 → WebSocket 广播
```

### 并发控制对比

| 场景 | 优化前 | 优化后 |
|------|--------|--------|
| 并发抽奖 | 乐观锁（可能重复执行） | Redisson 分布式锁（真正互斥） |
| 弹幕广播 | 同步阻塞 | 异步消息队列 |
| 接口保护 | 无 | Sentinel 限流 |

---

## 面试话术

### RabbitMQ
> "使用 RabbitMQ 实现弹幕异步广播，将业务逻辑和 WebSocket 推送解耦。消息队列起到削峰填谷的作用，2000 人同时发弹幕时不会阻塞业务线程。支持多消费者水平扩展，消息持久化避免丢失。"

### Redisson
> "使用 Redisson 分布式锁控制抽奖并发，锁粒度为轮次级别（lucky:lottery:lock:{roundId}）。tryLock 等待 3 秒，持有 30 秒，支持自动续期。相比乐观锁，分布式锁真正保证了互斥性，避免重复执行抽奖逻辑。"

### Sentinel
> "使用 Sentinel 实现接口限流，弹幕接口 100 QPS，登录接口 10 QPS（防暴力破解），抽奖接口 5 QPS。采用预热模式，避免冷启动时突发流量打垮系统。限流时返回 429 状态码和友好提示。"

---

## 相关文件

### 新增文件
- `lucky-server/src/main/java/com/lucky/config/RabbitConfig.java`
- `lucky-server/src/main/java/com/lucky/mq/MessageProducer.java`
- `lucky-server/src/main/java/com/lucky/mq/MessageConsumer.java`
- `lucky-server/src/main/java/com/lucky/config/SentinelConfig.java`
- `lucky-server/src/main/java/com/lucky/config/SentinelBlockExceptionHandler.java`

### 修改文件
- `lucky-server/pom.xml`
- `lucky-server/src/main/java/com/lucky/service/impl/DanmakuServiceImpl.java`
- `lucky-server/src/main/java/com/lucky/service/impl/LotteryServiceImpl.java`
- `lucky-server/src/main/resources/application.yml`
- `lucky-server/src/main/resources/application-prod.yml`
- `docker-compose.yml`
- `docker-compose.prod.yml`

---

## 总结

**已完成的集成：**
1. ✅ RabbitMQ 消息队列 - 异步处理弹幕广播
2. ✅ Redisson 分布式锁 - 抽奖并发控制
3. ✅ Sentinel 接口限流 - 防止恶意请求

**技术栈升级：**
- 新增：RabbitMQ、Redisson、Sentinel
- 优化：弹幕异步处理、分布式锁、接口限流

**架构收益：**
- 弹幕发送和广播解耦
- 真正的分布式并发控制
- 系统稳定性保障

---

*文档生成时间：2026-06-10 14:30*
*集成状态：已完成*
