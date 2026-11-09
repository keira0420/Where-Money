package shuhuai.wheremoney.response.bill;

import java.util.List;
import java.util.Map;

/**
 * 账单统计响应类
 * 用于返回支出和收入的统计数据
 */
public class StatisticResponse {
    private List<Map<String, Object>> payStatistic;
    private List<Map<String, Object>> incomeStatistic;

    /**
     * 构造方法
     *
     * @param payStatistic     支出统计数据列表
     * @param incomeStatistic  收入统计数据列表
     */
    public StatisticResponse(List<Map<String, Object>> payStatistic, List<Map<String, Object>> incomeStatistic) {
        this.payStatistic = payStatistic;
        this.incomeStatistic = incomeStatistic;
    }

    /**
     * 获取支出统计数据
     *
     * @return 支出统计数据列表
     */
    public List<Map<String, Object>> getPayStatistic() {
        return payStatistic;
    }

    /**
     * 设置支出统计数据
     *
     * @param payStatistic 支出统计数据列表
     */
    public void setPayStatistic(List<Map<String, Object>> payStatistic) {
        this.payStatistic = payStatistic;
    }

    /**
     * 获取收入统计数据
     *
     * @return 收入统计数据列表
     */
    public List<Map<String, Object>> getIncomeStatistic() {
        return incomeStatistic;
    }

    /**
     * 设置收入统计数据
     *
     * @param incomeStatistic 收入统计数据列表
     */
    public void setIncomeStatistic(List<Map<String, Object>> incomeStatistic) {
        this.incomeStatistic = incomeStatistic;
    }
}