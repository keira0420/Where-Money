package shuhuai.wheremoney.service.ai;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import shuhuai.wheremoney.service.excep.ai.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class AiRateLimiterTest {
    @Test void boundaryAndRedisFailure() {
        var redis=mock(StringRedisTemplate.class);
        var limiter=new AiRateLimiter(redis);
        ReflectionTestUtils.setField(limiter,"limit",5);
        when(redis.execute(any(org.springframework.data.redis.core.script.RedisScript.class),anyList())).thenReturn(5L,6L);
        assertDoesNotThrow(()->limiter.check(1));
        assertThrows(AiRateLimitException.class,()->limiter.check(1));
        when(redis.execute(any(org.springframework.data.redis.core.script.RedisScript.class),anyList())).thenThrow(new RuntimeException("offline"));
        assertThrows(AiInvokeException.class,()->limiter.check(1));
    }
}
