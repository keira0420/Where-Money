package shuhuai.wheremoney.mapper;

import org.apache.ibatis.annotations.Mapper;
import shuhuai.wheremoney.entity.PayBill;

import java.sql.Timestamp;
import java.util.List;

/**
 * 支出账单Mapper接口
 * 用于操作数据库中的pay_bill表，提供支出账单的CRUD操作
 */
@Mapper
public interface PayBillMapper {
    /**
     * 选择性插入支出账单
     * @param payBill 支出账单实体
     */
    void insertPayBillSelective(PayBill payBill);

    /**
     * 根据ID删除支出账单
     * @param id 支出账单ID
     * @return 影响的行数
     */
    Integer deletePayBillById(Integer id);

    /**
     * 根据ID选择性更新支出账单
     * @param payBill 支出账单实体
     * @return 影响的行数
     */
    Integer updatePayBillByIdSelective(PayBill payBill);

    /**
     * 根据ID更新支出账单
     * @param payBill 支出账单实体
     * @return 影响的行数
     */
    Integer updatePayBillById(PayBill payBill);

    /**
     * 根据账本ID查询支出账单
     * @param bookId 账本ID
     * @return 支出账单列表
     */
    List<PayBill> selectPayBillByBookId(Integer bookId);

    /**
     * 根据账本ID和时间范围查询支出账单
     * @param bookId 账本ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 支出账单列表
     */
    List<PayBill> selectPayBillByBookIdTime(Integer bookId, Timestamp startTime, Timestamp endTime);

    /**
     * 根据ID查询支出账单
     * @param id 支出账单ID
     * @return 支出账单实体
     */
    PayBill selectPayBillById(Integer id);

    /**
     * 根据ID查询支出账单（加锁）
     * @param id 支出账单ID
     * @return 支出账单实体
     */
    PayBill selectPayBillByIdForUpdate(Integer id);

    /**
     * 根据账本ID和分类ID查询支出账单
     * @param bookId 账本ID
     * @param billCategoryId 分类ID
     * @return 支出账单列表
     */
    List<PayBill> selectPayBillByBookIdCategory(Integer bookId, Integer billCategoryId);

    /**
     * 根据账本ID和分类ID查询支出账单ID列表
     * @param bookId 账本ID
     * @param billCategoryId 分类ID
     * @return 支出账单ID列表
     */
    List<Integer> selectPayBillIdsByBookIdCategory(Integer bookId, Integer billCategoryId);

    /**
     * 根据账本ID和旧分类ID更新支出账单分类
     * @param bookId 账本ID
     * @param oldBillCategoryId 旧分类ID
     * @param newBillCategoryId 新分类ID
     * @return 影响的行数
     */
    Integer updatePayBillCategoryByBookIdCategory(Integer bookId, Integer oldBillCategoryId, Integer newBillCategoryId);

    /**
     * 根据账本ID删除支出账单
     * @param bookId 账本ID
     * @return 影响的行数
     */
    Integer deletePayBillByBookId(Integer bookId);
}