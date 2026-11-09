package shuhuai.wheremoney.mapper;

import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface CacheEvictionMapper {
    record Task(long id, String cacheKey) {}
    @Insert("INSERT INTO cache_eviction_task(cache_key) VALUES(#{key})")
    void enqueue(String key);
    @Select("SELECT id,cache_key cacheKey FROM cache_eviction_task WHERE next_retry <= CURRENT_TIMESTAMP ORDER BY id LIMIT 100")
    List<Task> pending();
    @Delete("DELETE FROM cache_eviction_task WHERE id=#{id}")
    void complete(long id);
    @Update("UPDATE cache_eviction_task SET attempts=attempts+1,next_retry=TIMESTAMPADD(SECOND,LEAST(300,5*(attempts+1)),CURRENT_TIMESTAMP) WHERE id=#{id}")
    void retry(long id);
}
