package shuhuai.wheremoney.response.budget;

import shuhuai.wheremoney.entity.Budget;

import java.util.List;

/**
 * 获取所有预算响应类
 * 用于返回账本的所有预算信息
 */
public class GetAllBudgetResponse {
    private List<Budget> budgetList;

    /**
     * 构造方法
     *
     * @param budgetList 预算列表
     */
    public GetAllBudgetResponse(List<Budget> budgetList) {
        this.budgetList = budgetList;
    }

    /**
     * 获取预算列表
     *
     * @return 预算列表
     */
    public List<Budget> getBudgetList() {
        return budgetList;
    }

    /**
     * 设置预算列表
     *
     * @param budgetList 预算列表
     */
    public void setBudgetList(List<Budget> budgetList) {
        this.budgetList = budgetList;
    }
}