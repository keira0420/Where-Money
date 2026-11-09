package shuhuai.wheremoney.mapper;

import org.apache.ibatis.annotations.Mapper;
import shuhuai.wheremoney.entity.Budget;

import java.math.BigDecimal;
import java.util.List;

/**
 * 预算Mapper接口
 * 用于操作数据库中的budget表，提供预算的CRUD操作
 */
@Mapper
public interface BudgetMapper {
    /**
     * 插入预算
     * @param budget 预算实体
     * @return 影响的行数
     */
    Integer insertBudget(Budget budget);

    /**
     * 根据分类ID更新已用预算相对值
     * @param billCategoryId 账单分类ID
     * @param relativeValue 相对值
     * @return 影响的行数
     */
    Integer updateUsedRelativeByCategoryId(Integer billCategoryId, BigDecimal relativeValue);

    /**
     * 根据分类ID更新预算使用次数相对值
     * @param billCategoryId 账单分类ID
     * @param relativeValue 相对值
     * @return 影响的行数
     */
    Integer updateTimesRelativeByCategoryId(Integer billCategoryId, Integer relativeValue);

    /**
     * 根据账本ID查询预算
     * @param bookId 账本ID
     * @return 预算列表
     */
    List<Budget> selectBudgetsByBook(Integer bookId);

    /**
     * 根据账本ID查询预算限额总和
     * @param bookId 账本ID
     * @return 预算限额总和
     */
    BigDecimal selectLimitSumByBook(Integer bookId);

    /**
     * 根据ID查询预算
     * @param id 预算ID
     * @return 预算实体
     */
    Budget selectBudgetById(Integer id);

    /**
     * 根据ID更新预算
     * @param budget 预算实体
     * @return 影响的行数
     */
    Integer updateBudgetById(Budget budget);

    /**
     * 根据分类ID查询预算
     * @param categoryId 分类ID
     * @return 预算实体
     */
    Budget selectBudgetByCategoryId(Integer categoryId);

    /**
     * 根据ID删除预算
     * @param id 预算ID
     * @return 影响的行数
     */
    Integer deleteBudgetById(Integer id);

    /**
     * 根据账本ID删除预算
     * @param bookId 账本ID
     * @return 影响的行数
     */
    Integer deleteBudgetByBookId(Integer bookId);
}