package shuhuai.wheremoney.response.book;

import lombok.AllArgsConstructor;

import java.math.BigDecimal;

/**
 * 账本统计金额响应类
 * 用于返回账本的统计金额数据
 */
@AllArgsConstructor
public class StatisticAmountResponse {
    private BigDecimal amount;

    /**
     * 获取统计金额
     *
     * @return 统计金额
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * 设置统计金额
     *
     * @param amount 统计金额
     */
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}