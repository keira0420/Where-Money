package shuhuai.wheremoney.response.bill;

/**
 * 最大最小账单响应类
 * 用于返回最大和最小金额的账单信息
 */
public class MaxMinResponse {
    private BaseGetBillResponse max;
    private BaseGetBillResponse min;

    /**
     * 构造方法
     *
     * @param max 最大金额账单
     * @param min 最小金额账单
     */
    public MaxMinResponse(BaseGetBillResponse max, BaseGetBillResponse min) {
        this.max = max;
        this.min = min;
    }

    /**
     * 获取最大金额账单
     *
     * @return 最大金额账单
     */
    public BaseGetBillResponse getMax() {
        return max;
    }

    /**
     * 设置最大金额账单
     *
     * @param max 最大金额账单
     */
    public void setMax(BaseGetBillResponse max) {
        this.max = max;
    }

    /**
     * 获取最小金额账单
     *
     * @return 最小金额账单
     */
    public BaseGetBillResponse getMin() {
        return min;
    }

    /**
     * 设置最小金额账单
     *
     * @param min 最小金额账单
     */
    public void setMin(BaseGetBillResponse min) {
        this.min = min;
    }
}