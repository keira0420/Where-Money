package shuhuai.wheremoney.service;

import shuhuai.wheremoney.entity.BillCategory;
import shuhuai.wheremoney.entity.Book;
import shuhuai.wheremoney.type.BillType;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

/**
 * 账本服务接口
 * 提供账本相关的业务逻辑操作，包括账本的创建、查询、删除，以及账单分类管理和统计功能
 */
public interface BookService {
    /**
     * 添加账本
     * @param userId 用户ID
     * @param title 账本标题
     * @param beginDate 开始日期
     */
    void addBook(Integer userId, String title, Integer beginDate);

    /**
     * 获取用户的账本列表
     * @param userId 用户ID
     * @return 账本列表
     */
    List<Book> getBookList(Integer userId);

    /**
     * 获取指定账本
     * @param id 账本ID
     * @return 账本实体
     */
    Book getBook(Integer id);

    /**
     * 获取账本月支出
     * @param bookId 账本ID
     * @param month 月份
     * @return 支出金额
     */
    BigDecimal getPayMonth(Integer bookId, Timestamp month);

    /**
     * 获取账本月收入
     * @param bookId 账本ID
     * @param month 月份
     * @return 收入金额
     */
    BigDecimal getIncomeMonth(Integer bookId, Timestamp month);

    /**
     * 获取账本月结余
     * @param bookId 账本ID
     * @param month 月份
     * @return 结余金额
     */
    BigDecimal getBalanceMonth(Integer bookId, Timestamp month);

    /**
     * 获取账本月退款
     * @param bookId 账本ID
     * @param month 月份
     * @return 退款金额
     */
    BigDecimal getRefundMonth(Integer bookId, Timestamp month);

    /**
     * 获取所有账单分类
     * @param bookId 账本ID
     * @param type 账单类型
     * @return 账单分类列表
     */
    List<BillCategory> getAllBillCategory(Integer bookId, BillType type);

    /**
     * 添加账单分类
     * @param bookId 账本ID
     * @param billCategoryName 分类名称
     * @param svg 分类图标
     * @param type 分类类型
     */
    void addBillCategory(Integer bookId, String billCategoryName, String svg, BillType type);

    /**
     * 删除账单分类
     * @param billCategoryId 分类ID
     */
    void deleteBillCategory(Integer billCategoryId);

    /**
     * 更新账单分类
     * @param id 分类ID
     * @param billCategoryName 分类名称
     * @param svg 分类图标
     * @param type 分类类型
     * @param bookId 账本ID
     */
    void updateBillCategory(Integer id, String billCategoryName, String svg, BillType type, Integer bookId);

    /**
     * 删除账本
     * @param id 账本ID
     * @param userId 用户ID
     */
    void deleteBook(Integer id, Integer userId);
}