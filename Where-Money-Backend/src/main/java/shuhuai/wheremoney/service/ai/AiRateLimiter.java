package shuhuai.wheremoney.service.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import shuhuai.wheremoney.service.excep.ai.AiInvokeException;
import shuhuai.wheremoney.service.excep.ai.AiRateLimitException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AiRateLimiter {
    private final StringRedisTemplate redis;
    @Value("${ai.bill.parse.limit-per-minute:5}")
    private int limit;
    private static final DefaultRedisScript<Long> SCRIPT = new DefaultRedisScript<>("""
        local count = redis.call('INCR', KEYS[1])
        if count == 1 then redis.call('EXPIRE', KEYS[1], 120) end
        return count
        """, Long.class);

    public void check(Integer userId) {
        if (limit <= 0) return;
        String key = "ai:quota:" + userId + ":" + System.currentTimeMillis() / 60000;
        Long count;
        try {
            count = redis.execute(SCRIPT, List.of(key));
        } catch (Exception e) {
            throw new AiInvokeException("限流服务暂不可用，请稍后重试");
        }
        if (count == null) throw new AiInvokeException("限流服务暂不可用");
        if (count > limit) throw new AiRateLimitException("每分钟最多调用" + limit + "次，请稍后重试");
    }
}
