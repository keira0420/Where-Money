# 架构与实现

## 技术选型

| 选择 | 原因与代价 |
| --- | --- |
| Java21 + Spring MVC | 和原项目一致；虚拟线程用于模型阻塞I/O，但HTTP调用线程仍等待结果 |
| Spring AI ChatClient | Spring配置和模型调用统一；OpenAI兼容协议便于切换服务商 |
| MyBatis + MySQL/InnoDB | 金额事务、外键、行锁、唯一约束和聚合SQL都可明确控制 |
| Redis | 可丢弃的业务缓存、跨实例限流、模型结果复用；不保存唯一账务事实 |
| 不引入MQ | 记账是短事务；当前失效补偿任务量可由MySQL任务表+定时轮询处理 |
| Vue3 + Naive UI | 复用原有表单、图表和响应式布局，提供人工确认 |

## 模块入口

- `controller/AiAssistantController`：JSON接口。
- `service/impl/AiBillServiceImpl`：候选数据、Prompt、模型结果解析、实体映射。
- `service/ai/AiAccessGuard`：MySQL直接检查账本归属。
- `config/ResourceAccessInterceptor`：旧表单接口的账户、账单、账本、预算、分类权限检查。
- `service/ai/AiRateLimiter`：StringRedisTemplate执行Lua，计数和TTL原子完成。
- `service/ai/AiModelGateway`：温度0.1、关闭思考、虚拟线程、每实例并发信号量、20秒等待上限。
- `service/ai/AiConfirmationService`：最终校验、提交编号、数据库事务。
- `service/impl/BillServiceImpl`：复用原有支出、收入、转账、退款逻辑。
- `service/ai/AiFactsService`：可重复读只读快照，独立查询本月/上月/分类统计；事务结束后再调用模型，不长时间占用数据库连接。
- `service/ai/AiReportService`：数据指纹缓存与文字解读，失败显式降级。
- `service/cache/CacheInvalidator`：事务内记录待失效Key，提交后删缓存，后台重试。

## 数据表

| 表 | 作用 |
| --- | --- |
| user | BCrypt密码、账号、创建时间 |
| book | 所属用户、周期起始日、预算汇总 |
| asset | 所属用户、账户类型、余额、信用额度等 |
| bill_category | 所属账本、分类名、收支类型 |
| pay_bill / income_bill / transfer_bill / refund_bill | 不同账单字段与退款关联 |
| budget | 账本分类预算、限额与已用值 |
| ai_bill_parse_log / ai_report_log | 状态、耗时等审计；解析原文和模型原始响应默认不保存 |
| ai_bill_confirmation | `(user_id,request_id)`唯一主键、载荷摘要、已保存标记 |
| cache_eviction_task | 缓存Key、重试次数、下次执行时间 |

金额使用Java BigDecimal、MySQL DECIMAL(10,2)。拒绝超过两位小数和范围的输入，不做静默四舍五入。

SQL使用`#{}`绑定参数。时间范围采用`book_id=? AND bill_time>=? AND bill_time<?`，配合`(book_id,bill_time)`复合索引；月底使用半开区间避免最后一秒/毫秒遗漏。

## 幂等与并发

前端每份草稿生成UUID。确认时先完成字段与权限校验，在同一个事务中：

1. 插入提交编号；重复键通过`ON DUPLICATE KEY UPDATE`取得排他锁。
2. 当前读校验载荷摘要。相同编号不同内容返回错误。
3. 已保存则直接返回；未保存则写账单并标记saved。
4. 任一步失败，账单、余额、预算、提交记录与缓存任务一起回滚。

曾测试发现`INSERT IGNORE`多个并发重复请求后再`SELECT FOR UPDATE`会产生锁升级死锁，因此改为上述方式。摘要MD5仅用于载荷比较，不用于密码或安全签名。

新增账单先锁账本，再按ID排序锁转入/转出账户，减少锁顺序冲突。余额采用SQL相对更新`balance=balance+?`。修改/删除账单在事务中读取行锁版本；退款锁原支出并检查可退金额。复杂交叉操作仍可能由InnoDB检测死锁并回滚，返回可重试冲突，不能承诺永不死锁。

默认事务隔离使用READ_COMMITTED，月报多次聚合显式使用REPEATABLE_READ快照。不同隔离级别按读写需求选择。

## 缓存一致性

普通读：读取Redis一次，未命中查询MySQL，写入最长300秒固定TTL。事务内绕过业务缓存，不从陈旧缓存做账务判断，也不把未提交结果写回Redis。

普通写：数据库事务内记录失效任务；成功提交后删除缓存。后台每5秒扫描任务、再次幂等删除并清理成功任务。失败时保留任务并退避重试；应用在提交后崩溃，重启仍可继续处理。事务回滚不会触发afterCommit，任务记录也随事务回滚。

这是一种最终一致性方案。并发缓存未命中仍可能把提交前读到的旧值回填；后台再次失效和固定TTL限制影响窗口，不是跨MySQL/Redis的强事务。需要严格最新数据的余额变更、退款额度、权限判断都直接访问MySQL。

任务表不是消息中间件，没有消费者组、跨服务订阅或MQ事务消息。如果任务量大，可演进到Outbox发布MQ或CDC订阅，而不是在单体项目中为了关键词引入MQ。

## 限流与缓存的区别

限流Key为`ai:quota:{userId}:{epochMinute}`，按用户共享解析和未缓存月报的额度。Lua中的INCR与首次EXPIRE原子执行，TTL120秒清理。固定窗口在分钟边界可能短时突发；每实例信号量另外限制在途模型请求。多副本总并发上限不是单个信号量能解决的。

Redis限流不可用时拒绝新的模型调用以保护成本；普通业务缓存不可用时回源MySQL。月报缓存失败不影响真实统计。

## 统计与预算口径

AI月报采用自然月现金流：净支出=支出原额-当月实际到账退款+转账手续费，结余=收入-净支出；转账本金不计收支。分类Top5按支出原额排序。

预算沿用原账本周期实现：起始日为1至28日，统计周期内支出及相关周期退款，超支提醒不由模型判断。历史首页部分统计沿用原有净支出口径；跨月退款时与AI现金流月报可能有差异，尚需统一全部历史统计接口。不得把这两种口径混用解释为完全一致。

## 安全与可观测性

JWT签名密钥使用环境变量；local/test有明确的开发密钥，不应部署生产。ThreadLocal由拦截器和请求Filter清理。读取资源时使用数据库归属校验，不仅验证登录。

注册密码使用BCrypt随机盐。旧MD5账号在成功登录后迁移；不在浏览器Cookie中保存明文密码。日志只记录方法、路径、业务状态和请求ID，不记录查询参数或完整输入。原有下载系统日志接口默认拒绝访问。

HTTP响应保留旧项目业务包装`{code,message,data}`，不少业务错误仍返回HTTP200+业务码。这是兼容决策，不应宣称已经全面REST规范化；统一HTTP状态码可以作为后续API版本升级。

## 部署前仍需做的事

HTTPS、非root数据库服务账号、限流压测、灾备与恢复演练、密码修改后的Token撤销、登录反暴力限制、全量分页查询改造、依赖漏洞升级、完整历史统计口径统一、附件对象存储和文件内容校验。项目可用于面试展示，但没有线上SLA或未经验证的吞吐量承诺。
