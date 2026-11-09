package shuhuai.wheremoney.response.asset;

import java.util.List;
import java.util.Map;

/**
 * 资产日统计时间响应类
 * 用于返回资产的日统计数据
 */
public class DayStatisticTimeResponse {
    List<Map<String, Object>> dayStatistic;

    /**
     * 构造方法
     *
     * @param dayStatistic 日统计数据列表
     */
    public DayStatisticTimeResponse(List<Map<String, Object>> dayStatistic) {
        this.dayStatistic = dayStatistic;
    }

    /**
     * 获取日统计数据
     *
     * @return 日统计数据列表
     */
    public List<Map<String, Object>> getDayStatistic() {
        return dayStatistic;
    }

    /**
     * 设置日统计数据
     *
     * @param dayStatistic 日统计数据列表
     */
    public void setDayStatistic(List<Map<String, Object>> dayStatistic) {
        this.dayStatistic = dayStatistic;
    }
}