package shuhuai.wheremoney.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis配置类
 * 配置RedisTemplate和各种Redis操作对象，用于缓存和数据存储
 */
@Configuration
@EnableCaching
public class RedisConfig {
    /**
     * 配置RedisTemplate
     * 设置序列化器，确保Redis中的键值对能够正确序列化和反序列化
     * @param factory Redis连接工厂
     * @return 配置好的RedisTemplate实例
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // 设置连接工厂
        template.setConnectionFactory(factory);
        // 设置值序列化器，使用Jackson2JsonRedisSerializer
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        // 设置键序列化器，使用StringRedisSerializer
        template.setKeySerializer(new StringRedisSerializer());
        // 设置哈希键序列化器，使用StringRedisSerializer
        template.setHashKeySerializer(new StringRedisSerializer());
        // 设置哈希值序列化器，使用Jackson2JsonRedisSerializer
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        // 初始化RedisTemplate
        template.afterPropertiesSet();
        return template;
    }

    /**
     * 配置HashOperations
     * 用于操作Redis中的哈希结构
     * @param redisTemplate RedisTemplate实例
     * @return HashOperations实例
     */
    @Bean
    public HashOperations<String, String, Object> hashOperations(RedisTemplate<String, Object> redisTemplate) {
        return redisTemplate.opsForHash();
    }

    /**
     * 配置ValueOperations
     * 用于操作Redis中的字符串值
     * @param redisTemplate RedisTemplate实例
     * @return ValueOperations实例
     */
    @Bean
    public ValueOperations<String, Object> valueOperations(RedisTemplate<String, Object> redisTemplate) {
        return redisTemplate.opsForValue();
    }

    /**
     * 配置ListOperations
     * 用于操作Redis中的列表结构
     * @param redisTemplate RedisTemplate实例
     * @return ListOperations实例
     */
    @Bean
    public ListOperations<String, Object> listOperations(RedisTemplate<String, Object> redisTemplate) {
        return redisTemplate.opsForList();
    }

    /**
     * 配置SetOperations
     * 用于操作Redis中的集合结构
     * @param redisTemplate RedisTemplate实例
     * @return SetOperations实例
     */
    @Bean
    public SetOperations<String, Object> setOperations(RedisTemplate<String, Object> redisTemplate) {
        return redisTemplate.opsForSet();
    }

    /**
     * 配置ZSetOperations
     * 用于操作Redis中的有序集合结构
     * @param redisTemplate RedisTemplate实例
     * @return ZSetOperations实例
     */
    @Bean
    public ZSetOperations<String, Object> zSetOperations(RedisTemplate<String, Object> redisTemplate) {
        return redisTemplate.opsForZSet();
    }
}