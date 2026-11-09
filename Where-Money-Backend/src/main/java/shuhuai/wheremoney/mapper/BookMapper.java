package shuhuai.wheremoney.mapper;

import org.apache.ibatis.annotations.Mapper;
import shuhuai.wheremoney.entity.Book;
import shuhuai.wheremoney.entity.User;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

/**
 * 账本Mapper接口
 * 用于操作数据库中的book表，提供账本的CRUD操作和统计功能
 */
@Mapper
public interface BookMapper {
    /**
     * 选择性插入账本
     * @param book 账本实体
     * @return 影响的行数
     */
    Integer insertBookSelective(Book book);

    /**
     * 根据账本ID查询总预算
     * @param id 账本ID
     * @return 总预算金额
     */
    BigDecimal selectTotalBudgetByBook(Integer id);

    /**
     * 更新账本总预算
     * @param id 账本ID
     * @param totalBudget 总预算金额
     * @param usedBudget 已用预算金额
     */
    void updateTotalBudgetByBook(Integer id, BigDecimal totalBudget, BigDecimal usedBudget);

    /**
     * 根据账本ID更新已用预算相对值
     * @param id 账本ID
     * @param relativeValue 相对值
     * @return 影响的行数
     */
    Integer updateUsedBudgetRelativeById(Integer id, BigDecimal relativeValue);

    /**
     * 根据ID查询账本
     * @param id 账本ID
     * @return 账本实体
     */
    Book selectBookById(Integer id);

    /**
     * 根据用户查询账本
     * @param user 用户实体
     * @return 账本列表
     */
    List<Book> selectBookByUser(User user);

    /**
     * 根据用户和标题查询账本
     * @param user 用户实体
     * @param title 账本标题
     * @return 账本实体
     */
    Book selectBookByUserTitle(User user, String title);

    /**
     * 查询账本指定月份的支出
     * @param bookId 账本ID
     * @param month 月份
     * @return 支出金额
     */
    BigDecimal selectPayMonthByBookId(Integer bookId, Timestamp month);

    /**
     * 查询账本指定月份的收入
     * @param bookId 账本ID
     * @param month 月份
     * @return 收入金额
     */
    BigDecimal selectIncomeMonthByBookId(Integer bookId, Timestamp month);

    /**
     * 查询账本指定月份的结余
     * @param bookId 账本ID
     * @param month 月份
     * @return 结余金额
     */
    BigDecimal selectBalanceMonthByBookId(Integer bookId, Timestamp month);

    /**
     * 查询账本指定月份的退款
     * @param bookId 账本ID
     * @param month 月份
     * @return 退款金额
     */
    BigDecimal selectRefundMonthByBookId(Integer bookId, Timestamp month);

    /**
     * 删除账本
     * @param id 账本ID
     * @return 影响的行数
     */
    Integer deleteBook(Integer id);
}