# membership-demo

探探会员服务核心逻辑的 **Java 简化重构**(源自 `tantan-backend-membership` Go 代码)。

只保留三条最核心、面试最值得讲的业务规则,用最小可运行的 Spring Boot 项目实现。

## 一、怎么跑

```bash
cd membership-demo
mvn spring-boot:run
```

启动后 H2 控制台:`http://localhost:8080/h2-console`(JDBC URL 填 `jdbc:h2:mem:membership`)。

> 需要 JDK 17+,Maven,以及 IDE 安装 Lombok 插件(IntelliJ 自带)。

## 二、功能与接口

| 功能 | 接口 | 说明 |
|---|---|---|
| 会员开通/续费 | `POST /api/membership/open` | 参数 userId, type, durationSeconds, originalId |
| 会员状态查询 | `GET /api/membership/query` | 参数 userId, type |
| 加道具(发特权) | `POST /api/privilege/add` | 参数 userId, propType, number |
| 消耗道具(用特权) | `POST /api/privilege/consume` | 参数 userId, propType, number |
| 特权校验 | `GET /api/privilege/check` | 参数 userId, propType |
| 加币 | `POST /api/coin/add` | 参数 userId, amount, eventId |
| 扣币 | `POST /api/coin/deduct` | 参数 userId, amount, eventId |
| 查余额 | `GET /api/coin/balance` | 参数 userId |

## 三、三条核心业务规则(面试重点)

### 1. 幂等

- 会员开通用 `originalId`(订单号)唯一:同一订单重复调用,只开通一次。
- 加/扣币用 `eventId`(订单号)唯一:同一事件重复调用,只生效一次。

对应 Go 代码的 `SelectByOriginalIDOrInsert` 和钱包 `AddCoin` 的 `ErrAddOrderExist`(重复订单视为成功)。

### 2. 防超扣

- 消耗道具:`消耗数量 = min(请求数量, 剩余数量)`,余额不足只消耗剩余。
- 扣币:余额不足**直接拒绝**,不允许扣成负数。

对应 Go 代码 `UserProp.Use()` 里的 `if left < used { used = left }`,以及你简历里的"探币并发错误治理""SuperLike 道具超发修复"。

### 3. 续期 / 过期重开

会员开通时:`beginTime = max(now, 当前 endTime)`。

- 会员还有效 → 从当前到期时间**顺延**(续期)。
- 会员已过期/无记录 → 从现在**重新开始**。

对应 Go 代码 `AddMembership` 里的 `record.BeginTime = endTime; if BeginTime.Before(now) { BeginTime = now }`。

### 并发安全

所有写操作都在事务内,用**行级悲观锁**(`SELECT ... FOR UPDATE`,对应 Go 的 `pg_advisory_xact_lock` + `FOR UPDATE`),保证并发下不超扣、不重复。

## 四、演示幂等和防超扣(可以直接 curl 验证)

```bash
# 1. 发 10 个道具
curl -X POST "localhost:8080/api/privilege/add?userId=1&propType=superlike&number=10"
# 2. 想消耗 15 个,但只剩 10 个 → 防超扣,实际只消耗 10
curl -X POST "localhost:8080/api/privilege/consume?userId=1&propType=superlike&number=15"
# 3. 加币 100
curl -X POST "localhost:8080/api/coin/add?userId=1&amount=100&eventId=order-001"
# 4. 用同一个 eventId 再加 100 → 幂等,余额还是 100
curl -X POST "localhost:8080/api/coin/add?userId=1&amount=100&eventId=order-001"
# 5. 扣 200,但余额只有 100 → 防超扣,报"余额不足"
curl -X POST "localhost:8080/api/coin/deduct?userId=1&amount=200&eventId=order-002"
```

## 五、和原 Go 代码的差异(简化说明)

| 原 Go 实现 | 本 demo 简化 |
|---|---|
| PostgreSQL 分库分表 + `pg_advisory_xact_lock` | H2 单库 + 行级悲观锁 |
| 会员/探币/道具分散在多个微服务 + gRPC | 单应用 + REST |
| Redis 缓存会员到期时间 | 直接查库 |
| Kafka 异步流水 + 补偿 | 同步事务 |

这些简化不影响"幂等、防超扣、续期"这三条核心规则的表达,面试时你可以主动说:"生产环境是分库分表 + advisory lock + Redis 缓存 + Kafka 补偿,这个 demo 是我用 Java 把核心账本逻辑重新实现了一遍。"
