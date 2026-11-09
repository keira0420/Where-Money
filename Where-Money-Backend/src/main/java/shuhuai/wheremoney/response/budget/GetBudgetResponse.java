package shuhuai.wheremoney.response.budget;

import shuhuai.wheremoney.entity.Budget;

/**
 * 获取单个预算响应类
 * 用于返回单个预算的详细信息
 */
public class GetBudgetResponse {
    Budget budget;

    /**
     * 构造方法
     *
     * @param budget 预算对象
     */
    public GetBudgetResponse(Budget budget) {
        this.budget = budget;
    }

    /**
     * 获取预算对象
     *
     * @return 预算对象
     */
    public Budget getBudget() {
        return budget;
    }

    /**
     * 设置预算对象
     *
     * @param budget 预算对象
     */
    public void setBudget(Budget budget) {
        this.budget = budget;
    }
}