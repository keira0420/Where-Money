package shuhuai.wheremoney.mapper;

import org.apache.ibatis.annotations.Mapper;
import shuhuai.wheremoney.entity.AiBillParseLog;

/**
 * AI账单解析日志Mapper
 */
@Mapper
public interface AiBillParseLogMapper {
    /**
     * 插入AI账单解析日志
     *
     * @param log 日志
     * @return 影响行数
     */
    Integer insertAiBillParseLogSelective(AiBillParseLog log);
}
