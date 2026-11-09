package shuhuai.wheremoney.mapper;

import org.apache.ibatis.annotations.Mapper;
import shuhuai.wheremoney.entity.RefundBill;

import java.sql.Timestamp;
import java.util.List;

/**
 * 退款账单Mapper接口
 * 用于操作数据库中的refund_bill表，提供退款账单的CRUD操作
 */
@Mapper
public interface RefundBillMapper {
    RefundBill selectRefundBillByIdForUpdate(Integer id);
    /**
     * 选择性插入退款账单
     * @param refundBill 退款账单实体
     */
    void insertRefundBillSelective(RefundBill refundBill);

    /**
     * 根据ID删除退款账单
     * @param id 退款账单ID
     * @return 影响的行数
     */
    Integer deleteRefundBillById(Integer id);

    /**
     * 根据ID选择性更新退款账单
     * @param refundBill 退款账单实体
     * @return 影响的行数
     */
    Integer updateRefundBillByIdSelective(RefundBill refundBill);

    /**
     * 根据ID更新退款账单
     * @param refundBill 退款账单实体
     * @return 影响的行数
     */
    Integer updateRefundBillById(RefundBill refundBill);

    /**
     * 根据账本ID查询退款账单
     * @param bookId 账本ID
     * @return 退款账单列表
     */
    List<RefundBill> selectRefundBillByBookId(Integer bookId);

    /**
     * 根据账本ID和时间范围查询退款账单
     * @param bookId 账本ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 退款账单列表
     */
    List<RefundBill> selectRefundBillByBookIdTime(Integer bookId, Timestamp startTime, Timestamp endTime);

    /**
     * 根据ID查询退款账单
     * @param id 退款账单ID
     * @return 退款账单实体
     */
    RefundBill selectRefundBillById(Integer id);

    /**
     * 根据支出账单ID查询退款账单
     * @param payBillId 支出账单ID
     * @return 退款账单列表
     */
    List<RefundBill> selectRefundBillByPayBillId(Integer payBillId);

    /**
     * 根据账本ID删除退款账单
     * @param bookId 账本ID
     * @return 影响的行数
     */
    Integer deleteRefundBillByBookId(Integer bookId);
}
