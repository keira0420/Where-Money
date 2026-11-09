package shuhuai.wheremoney.mapper;

import org.apache.ibatis.annotations.Mapper;
import shuhuai.wheremoney.entity.TransferBill;

import java.sql.Timestamp;
import java.util.List;

/**
 * 转账账单Mapper接口
 * 用于操作数据库中的transfer_bill表，提供转账账单的CRUD操作
 */
@Mapper
public interface TransferBillMapper {
    TransferBill selectTransferBillByIdForUpdate(Integer id);
    /**
     * 选择性插入转账账单
     * @param transferBill 转账账单实体
     */
    void insertTransferBillSelective(TransferBill transferBill);

    /**
     * 根据ID删除转账账单
     * @param id 转账账单ID
     * @return 影响的行数
     */
    Integer deleteTransferBillById(Integer id);

    /**
     * 根据ID选择性更新转账账单
     * @param transferBill 转账账单实体
     * @return 影响的行数
     */
    Integer updateTransferBillByIdSelective(TransferBill transferBill);

    /**
     * 根据ID更新转账账单
     * @param transferBill 转账账单实体
     * @return 影响的行数
     */
    Integer updateTransferBillById(TransferBill transferBill);

    /**
     * 根据账本ID查询转账账单
     * @param bookId 账本ID
     * @return 转账账单列表
     */
    List<TransferBill> selectTransferBillByBookId(Integer bookId);

    /**
     * 根据账本ID和时间范围查询转账账单
     * @param bookId 账本ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 转账账单列表
     */
    List<TransferBill> selectTransferBillByBookIdTime(Integer bookId, Timestamp startTime, Timestamp endTime);

    /**
     * 根据ID查询转账账单
     * @param id 转账账单ID
     * @return 转账账单实体
     */
    TransferBill selectTransferBillById(Integer id);

    /**
     * 根据账本ID删除转账账单
     * @param bookId 账本ID
     * @return 影响的行数
     */
    Integer deleteTransferBillByBookId(Integer bookId);
}
