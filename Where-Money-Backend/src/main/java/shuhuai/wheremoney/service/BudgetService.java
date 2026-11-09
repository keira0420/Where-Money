package shuhuai.wheremoney.service;

import shuhuai.wheremoney.entity.Budget;

import java.math.BigDecimal;
import java.util.List;

/**
 * 预算服务接口
 * 提供预算相关的业务逻辑操作，包括预算的创建、查询、更新、删除，以及预算统计和验证功能
 */
public interface BudgetService {
    /**
     * 添加预算
     * @param bookId 账本ID
     * @param billCategoryId 账单分类ID
     * @param limit 预算限额
     */
    void addBudget(Integer bookId, Integer billCategoryId, BigDecimal limit);

    /**
     * 更新预算
     * @param budget 预算实体
     */
    void updateBudget(Budget budget);

    /**
     * 添加预算（带验证）
     * @param bookId 账本ID
     * @param billCategoryId 账单分类ID
     * @param limit 预算限额
     */
    void addBudgetValidated(Integer bookId, Integer billCategoryId, BigDecimal limit);

    /**
     * 更新预算（带验证）
     * @param budgetId 预算ID
     * @param billCategoryId 账单分类ID
     * @param limit 预算限额
     * @param amount 预算金额
     * @param times 预算使用次数
     */
    void updateBudgetValidated(Integer budgetId, Integer billCategoryId, BigDecimal limit, BigDecimal amount, Integer times);

    /**
     * 获取指定预算
     * @param id 预算ID
     * @return 预算实体
     */
    Budget getBudget(Integer id);

    /**
     * 获取账本的所有预算
     * @param bookId 账本ID
     * @return 预算列表
     */
    List<Budget> getBudgetsByBook(Integer bookId);

    /**
     * 获取账本的总预算
     * @param bookId 账本ID
     * @return 总预算金额
     */
    BigDecimal getTotalBudgetByBook(Integer bookId);

    /**
     * 获取账本的已分配预算
     * @param bookId 账本ID
     * @return 已分配预算金额
     */
    BigDecimal getAllocatedLimitByBook(Integer bookId);

    /**
     * 更新账本的总预算
     * @param bookId 账本ID
     * @param totalBudget 总预算金额
     * @param usedBudget 已用预算金额
     */
    void updateTotalBudgetByBook(Integer bookId, BigDecimal totalBudget, BigDecimal usedBudget);

    /**
     * 更新账本的总预算（带验证）
     * @param bookId 账本ID
     * @param totalBudget 总预算金额
     * @param usedBudget 已用预算金额
     * @return 是否更新成功
     */
    boolean updateTotalBudgetValidated(Integer bookId, BigDecimal totalBudget, BigDecimal usedBudget);

    /**
     * 根据分类ID获取预算
     * @param billCategoryId 分类ID
     * @return 预算实体
     */
    Budget selectBudgetByCategoryId(Integer billCategoryId);

    /**
     * 重建账本的预算
     * @param bookId 账本ID
     */
    void rebuildBudgetByBook(Integer bookId);

    /**
     * 更新总已用预算相对值
     * @param id 预算ID
     * @param relativeValue 相对值
     */
    void changeTotalUsedBudgetRelative(Integer id, BigDecimal relativeValue);

    /**
     * 更新分类已用预算相对值
     * @param billCategoryId 分类ID
     * @param relativeValue 相对值
     */
    void changeCategoryUsedBudgetRelative(Integer billCategoryId, BigDecimal relativeValue);

    /**
     * 更新分类预算使用次数相对值
     * @param billCategoryId 分类ID
     * @param relativeValue 相对值
     */
    void changeCategoryTimesRelative(Integer billCategoryId, Integer relativeValue);

    /**
     * 删除预算
     * @param id 预算ID
     */
    void deleteBudget(Integer id);
}