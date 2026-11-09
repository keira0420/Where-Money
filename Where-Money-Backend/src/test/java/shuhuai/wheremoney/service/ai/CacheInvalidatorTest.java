package shuhuai.wheremoney.service.ai;

import org.junit.jupiter.api.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import shuhuai.wheremoney.mapper.CacheEvictionMapper;
import shuhuai.wheremoney.service.cache.CacheInvalidator;
import java.util.List;
import static org.mockito.Mockito.*;

class CacheInvalidatorTest {
    @AfterEach void clear() { TransactionSynchronizationManager.clear(); }

    @Test void deleteRunsOnlyAfterCommit() {
        var tasks=mock(CacheEvictionMapper.class);var redis=mock(StringRedisTemplate.class);
        var invalidator=new CacheInvalidator(tasks,redis);
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);
        invalidator.evict("asset:1");
        verify(tasks).enqueue("asset:1");verifyNoInteractions(redis);
        for(var synchronization:TransactionSynchronizationManager.getSynchronizations()) synchronization.afterCommit();
        verify(redis).delete("asset:1");
    }

    @Test void failedDeleteIsRetriedAndCompletedAfterRecovery() {
        var tasks=mock(CacheEvictionMapper.class);var redis=mock(StringRedisTemplate.class);
        var invalidator=new CacheInvalidator(tasks,redis);
        when(tasks.pending()).thenReturn(List.of(new CacheEvictionMapper.Task(1,"asset:1")));
        when(redis.delete("asset:1")).thenThrow(new RuntimeException("offline")).thenReturn(true);
        invalidator.drain();verify(tasks).retry(1);verify(tasks,never()).complete(1);
        invalidator.drain();verify(tasks).complete(1);
    }
}
