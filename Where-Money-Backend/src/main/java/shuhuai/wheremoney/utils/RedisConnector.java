package shuhuai.wheremoney.utils;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis操作工具类
 * 提供Redis的各种操作方法，包括字符串、哈希、集合、列表等数据类型的操作
 */
@Component
public class RedisConnector {

    /**
     * Redis模板
     */
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private shuhuai.wheremoney.service.cache.CacheInvalidator invalidator;

    private boolean inTransaction() {
        return org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive();
    }

    /**
     * 构造方法
     *
     * @param redisTemplate Redis模板
     */
    public RedisConnector(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 设置键的过期时间
     *
     * @param key 键
     * @param time 过期时间（秒）
     * @return 是否设置成功
     */
    public Boolean setExpire(String key, long time) {
        try {
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取键的过期时间
     *
     * @param key 键
     * @return 过期时间（秒）
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 判断键是否存在
     *
     * @param key 键
     * @return 是否存在
     */
    public Boolean existObject(String key) {
        try {
            return !inTransaction() && Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 删除键
     *
     * @param key 键（可多个）
     */
    @SuppressWarnings("unchecked")
    public void deleteObject(String... key) {
        if (key != null) for (String item : key) invalidator.evict(item);
    }

    /**
     * 读取字符串值
     *
     * @param key 键
     * @return 值
     */
    public Object readObject(String key) {
        if (inTransaction() || key == null) return null;
        try { return redisTemplate.opsForValue().get(key); }
        catch (Exception e) { return null; }
    }

    /**
     * 写入字符串值
     *
     * @param key 键
     * @param value 值
     * @return 是否写入成功
     */
    public Boolean writeObject(String key, Object value) {
        return writeObject(key, value, 300L);
    }

    /**
     * 写入字符串值并设置过期时间
     *
     * @param key 键
     * @param value 值
     * @param time 过期时间（秒）
     * @return 是否写入成功
     */
    public Boolean writeObject(String key, Object value, Long time) {
        if (inTransaction()) {
            invalidator.evict(key);
            return true;
        }
        if (value == null) return false;
        try {
            redisTemplate.opsForValue().set(key, value, Math.max(1, Math.min(time == null ? 300 : time, 300)), TimeUnit.SECONDS);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 递增字符串值
     *
     * @param key 键
     * @param number 递增数量
     * @return 递增后的值
     */
    public Long increaseObject(String key, Long number) {
        return number > 0 ? redisTemplate.opsForValue().increment(key, number) : null;
    }

    /**
     * 递减字符串值
     *
     * @param key 键
     * @param number 递减数量
     * @return 递减后的值
     */
    public Long decreaseObject(String key, Long number) {
        return number > 0 ? redisTemplate.opsForValue().increment(key, -number) : null;
    }

    /**
     * 读取哈希表中的一个字段值
     *
     * @param key 键
     * @param item 字段
     * @return 值
     */
    public Object readMap(String key, String item) {
        return redisTemplate.opsForHash().get(key, item);
    }

    /**
     * 读取哈希表中的所有字段和值
     *
     * @param key 键
     * @return 字段和值的映射
     */
    public Map<Object, Object> readMap(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 写入哈希表
     *
     * @param key 键
     * @param map 字段和值的映射
     * @return 是否写入成功
     */
    public Boolean writeMap(String key, Map<String, Object> map) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 写入哈希表并设置过期时间
     *
     * @param key 键
     * @param map 字段和值的映射
     * @param time 过期时间（秒）
     * @return 是否写入成功
     */
    public Boolean writeMap(String key, Map<String, Object> map, Long time) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            if (time > 0) {
                setExpire(key, time);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 写入哈希表中的一个字段
     *
     * @param key 键
     * @param item 字段
     * @param value 值
     * @return 是否写入成功
     */
    public Boolean writeMap(String key, String item, Object value) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 写入哈希表中的一个字段并设置过期时间
     *
     * @param key 键
     * @param item 字段
     * @param value 值
     * @param time 过期时间（秒）
     * @return 是否写入成功
     */
    public Boolean writeMap(String key, String item, Object value, Long time) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            if (time > 0) {
                return setExpire(key, time);
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 删除哈希表中的字段
     *
     * @param key 键
     * @param item 字段（可多个）
     * @return null
     */
    public Void deleteMap(String key, Object... item) {
        redisTemplate.opsForHash().delete(key, item);
        return null;
    }

    /**
     * 判断哈希表中是否存在指定字段
     *
     * @param key 键
     * @param item 字段
     * @return 是否存在
     */
    public Boolean existMap(String key, String item) {
        return redisTemplate.opsForHash().hasKey(key, item);
    }

    /**
     * 递增哈希表中的字段值
     *
     * @param key 键
     * @param item 字段
     * @param number 递增数量
     * @return 递增后的值
     */
    public Double increaseMap(String key, String item, Double number) {
        return redisTemplate.opsForHash().increment(key, item, number);
    }

    /**
     * 递减哈希表中的字段值
     *
     * @param key 键
     * @param item 字段
     * @param number 递减数量
     * @return 递减后的值
     */
    public Double decreaseMap(String key, String item, Double number) {
        return redisTemplate.opsForHash().increment(key, item, -number);
    }

    /**
     * 读取集合中的所有元素
     *
     * @param key 键
     * @return 元素集合
     */
    public Set<Object> readSet(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 判断集合中是否存在指定元素
     *
     * @param key 键
     * @param value 元素
     * @return 是否存在
     */
    public Boolean existSet(String key, Object value) {
        try {
            return redisTemplate.opsForSet().isMember(key, value);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 写入集合
     *
     * @param key 键
     * @param values 元素（可多个）
     * @return 成功添加的元素数量
     */
    public Long writeSet(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().add(key, values);
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * 写入集合并设置过期时间
     *
     * @param key 键
     * @param time 过期时间（秒）
     * @param values 元素（可多个）
     * @return 成功添加的元素数量
     */
    public Long writeSet(String key, Long time, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().add(key, values);
            if (time > 0) {
                setExpire(key, time);
            }
            return count;
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * 获取集合大小
     *
     * @param key 键
     * @return 集合大小
     */
    public Long getSetSize(String key) {
        try {
            return redisTemplate.opsForSet().size(key);
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * 删除集合中的元素
     *
     * @param key 键
     * @param values 元素（可多个）
     * @return 成功删除的元素数量
     */
    public Long deleteSet(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().remove(key, values);
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * 读取列表中的指定范围元素
     *
     * @param key 键
     * @param start 开始索引
     * @param end 结束索引
     * @return 元素列表
     */
    public List<Object> readList(String key, Long start, Long end) {
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取列表大小
     *
     * @param key 键
     * @return 列表大小
     */
    public Long getListSize(String key) {
        try {
            return redisTemplate.opsForList().size(key);
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * 读取列表中指定索引的元素
     *
     * @param key 键
     * @param index 索引
     * @return 元素
     */
    public Object readList(String key, Long index) {
        try {
            return redisTemplate.opsForList().index(key, index);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 向右推入列表元素
     *
     * @param key 键
     * @param value 元素
     * @return 是否推入成功
     */
    public Boolean writeList(String key, Object value) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 向右推入列表元素并设置过期时间
     *
     * @param key 键
     * @param value 元素
     * @param time 过期时间（秒）
     * @return 是否推入成功
     */
    public Boolean writeList(String key, Object value, Long time) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            if (time > 0) {
                setExpire(key, time);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 向右推入多个列表元素
     *
     * @param key 键
     * @param value 元素列表
     * @return 是否推入成功
     */
    public Boolean writeList(String key, List<Object> value) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 向右推入多个列表元素并设置过期时间
     *
     * @param key 键
     * @param value 元素列表
     * @param time 过期时间（秒）
     * @return 是否推入成功
     */
    public Boolean writeList(String key, List<Object> value, Long time) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            if (time > 0) {
                setExpire(key, time);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 设置列表中指定索引的元素
     *
     * @param key 键
     * @param index 索引
     * @param value 元素
     * @return 是否设置成功
     */
    public Boolean writeList(String key, Long index, Object value) {
        try {
            redisTemplate.opsForList().set(key, index, value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 删除列表中的元素
     *
     * @param key 键
     * @param count 删除数量（正数：从左到右删除指定数量的元素；负数：从右到左删除指定数量的元素；0：删除所有匹配的元素）
     * @param value 元素
     * @return 成功删除的元素数量
     */
    public Long deleteList(String key, Long count, Object value) {
        try {
            return redisTemplate.opsForList().remove(key, count, value);
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * 获取匹配模式的键集合
     *
     * @param pattern 匹配模式
     * @return 键集合
     */
    public Set<String> getKeys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    /**
     * 发布消息
     *
     * @param channel 频道
     * @param message 消息
     */
    public void convertAndSend(String channel, Object message) {
        redisTemplate.convertAndSend(channel, message);
    }

    /**
     * 向右推入绑定列表元素
     *
     * @param listKey 键
     * @param values 元素（可多个）
     */
    public void pushList(String listKey, Object... values) {
        BoundListOperations<String, Object> boundValueOperations = redisTemplate.boundListOps(listKey);
        boundValueOperations.rightPushAll(values);
    }

    /**
     * 向右推入绑定列表元素并设置过期时间
     *
     * @param listKey 键
     * @param expireEnum 过期时间枚举
     * @param values 元素（可多个）
     */
    public void pushList(String listKey, Status.ExpireEnum expireEnum, Object... values) {
        BoundListOperations<String, Object> boundValueOperations = redisTemplate.boundListOps(listKey);
        boundValueOperations.rightPushAll(values);
        boundValueOperations.expire(expireEnum.getTime(), expireEnum.getTimeUnit());
    }

    /**
     * 读取绑定列表中的指定范围元素
     *
     * @param listKey 键
     * @param start 开始索引
     * @param end 结束索引
     * @return 元素列表
     */
    public List<Object> rangeList(String listKey, long start, long end) {
        BoundListOperations<String, Object> boundValueOperations = redisTemplate.boundListOps(listKey);
        return boundValueOperations.range(start, end);
    }

    /**
     * 从绑定列表右侧弹出元素
     *
     * @param listKey 键
     * @return 弹出的元素
     */
    public Object popList(String listKey) {
        BoundListOperations<String, Object> boundValueOperations = redisTemplate.boundListOps(listKey);
        return boundValueOperations.rightPop();
    }
}

/**
 * 状态抽象类
 */
abstract class Status {
    /**
     * 过期时间枚举
     */
    public enum ExpireEnum {
        /**
         * 未读消息过期时间：30天
         */
        UNREAD_MSG(30L, TimeUnit.DAYS);

        /**
         * 过期时间
         */
        private Long time;

        /**
         * 时间单位
         */
        private TimeUnit timeUnit;

        /**
         * 构造方法
         *
         * @param time 过期时间
         * @param timeUnit 时间单位
         */
        ExpireEnum(Long time, TimeUnit timeUnit) {
            this.time = time;
            this.timeUnit = timeUnit;
        }

        /**
         * 获取过期时间
         *
         * @return 过期时间
         */
        public Long getTime() {
            return time;
        }

        /**
         * 设置过期时间
         *
         * @param time 过期时间
         */
        public void setTime(Long time) {
            this.time = time;
        }

        /**
         * 获取时间单位
         *
         * @return 时间单位
         */
        public TimeUnit getTimeUnit() {
            return timeUnit;
        }

        /**
         * 设置时间单位
         *
         * @param timeUnit 时间单位
         */
        public void setTimeUnit(TimeUnit timeUnit) {
            this.timeUnit = timeUnit;
        }
    }
}
