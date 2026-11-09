package shuhuai.wheremoney.mapper;

import org.apache.ibatis.annotations.Mapper;
import shuhuai.wheremoney.entity.IncomeBill;

import java.sql.Timestamp;
import java.util.List;

/**
 * 收入账单Mapper接口
 * 用于操作数据库中的income_bill表，提供收入账单的CRUD操作
 */
@Mapper
public interface IncomeBillMapper {
    IncomeBill selectIncomeBillByIdForUpdate(Integer id);
    /**
     * 选择性插入收入账单
     * @param incomeBill 收入账单实体
     */
    void insertIncomeBillSelective(IncomeBill incomeBill);

    /**
     * 根据ID删除收入账单
     * @param id 收入账单ID
     * @return 影响的行数
     */
    Integer deleteIncomeBillById(Integer id);

    /**
     * 根据ID选择性更新收入账单
     * @param incomeBill 收入账单实体
     * @return 影响的行数
     */
    Integer updateIncomeBillByIdSelective(IncomeBill incomeBill);

    /**
     * 根据ID更新收入账单
     * @param incomeBill 收入账单实体
     * @return 影响的行数
     */
    Integer updateIncomeBillById(IncomeBill incomeBill);

    /**
     * 根据账本ID查询收入账单
     * @param bookId 账本ID
     * @return 收入账单列表
     */
    List<IncomeBill> selectIncomeBillByBookId(Integer bookId);

    /**
     * 根据账本ID和时间范围查询收入账单
     * @param bookId 账本ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 收入账单列表
     */
    List<IncomeBill> selectIncomeBillByBookIdTime(Integer bookId, Timestamp startTime, Timestamp endTime);

    /**
     * 根据ID查询收入账单
     * @param id 收入账单ID
     * @return 收入账单实体
     */
    IncomeBill selectIncomeBillById(Integer id);

    /**
     * 根据账本ID和分类ID查询收入账单
     * @param bookId 账本ID
     * @param billCategoryId 分类ID
     * @return 收入账单列表
     */
    List<IncomeBill> selectIncomeBillByBookIdCategory(Integer bookId, Integer billCategoryId);

    /**
     * 根据账本ID和分类ID查询收入账单ID列表
     * @param bookId 账本ID
     * @param billCategoryId 分类ID
     * @return 收入账单ID列表
     */
    List<Integer> selectIncomeBillIdsByBookIdCategory(Integer bookId, Integer billCategoryId);

    /**
     * 根据账本ID和旧分类ID更新收入账单分类
     * @param bookId 账本ID
     * @param oldBillCategoryId 旧分类ID
     * @param newBillCategoryId 新分类ID
     * @return 影响的行数
     */
    Integer updateIncomeBillCategoryByBookIdCategory(Integer bookId, Integer oldBillCategoryId, Integer newBillCategoryId);

    /**
     * 根据账本ID删除收入账单
     * @param bookId 账本ID
     * @return 影响的行数
     */
    Integer deleteIncomeBillByBookId(Integer bookId);
}
