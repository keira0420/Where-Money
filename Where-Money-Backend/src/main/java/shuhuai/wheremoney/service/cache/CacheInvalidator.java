package shuhuai.wheremoney.service.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import shuhuai.wheremoney.mapper.CacheEvictionMapper;

/** Durable invalidation outbox, not an MQ. Task insert shares the business transaction. */
@Component
@RequiredArgsConstructor
@Slf4j
public class CacheInvalidator {
    private final CacheEvictionMapper tasks;
    private final StringRedisTemplate redis;

    public void evict(String key) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            if (TransactionSynchronizationManager.isCurrentTransactionReadOnly()) return;
            tasks.enqueue(key);
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() {
                    try { redis.delete(key); }
                    catch (Exception e) { log.warn("缓存失效已进入持久化重试队列"); }
                }
            });
        } else {
            tasks.enqueue(key);
            try { redis.delete(key); }
            catch (Exception e) { log.warn("缓存失效等待后台重试"); }
        }
    }

    @Scheduled(fixedDelayString = "${cache.retry-delay-ms:5000}")
    public void drain() {
        try {
            for (var task : tasks.pending()) {
                try { redis.delete(task.cacheKey()); tasks.complete(task.id()); }
                catch (Exception e) { tasks.retry(task.id()); }
            }
        } catch (Exception e) { log.warn("缓存补偿任务暂不可用: {}", e.getClass().getSimpleName()); }
    }
}
