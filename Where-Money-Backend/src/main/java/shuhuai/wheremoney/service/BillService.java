package shuhuai.wheremoney.service;

import org.springframework.web.multipart.MultipartFile;
import shuhuai.wheremoney.entity.BaseBill;
import shuhuai.wheremoney.entity.IncomeBill;
import shuhuai.wheremoney.entity.PayBill;
import shuhuai.wheremoney.response.bill.BillImageResponse;
import shuhuai.wheremoney.type.BillType;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * 账单服务接口
 * 提供账单相关的业务逻辑操作，包括账单的创建、查询、更新、删除，以及账单统计和图片管理功能
 */
public interface BillService {
    /**
     * 添加账单
     * @param bookId 账本ID
     * @param inAssetId 收入资产ID
     * @param outAssetId 支出资产ID
     * @param payBillId 关联的支出账单ID（用于退款）
     * @param billCategoryId 账单分类ID
     * @param type 账单类型
     * @param amount 账单金额
     * @param transferFee 转账手续费
     * @param time 账单时间
     * @param remark 账单备注
     * @param refunded 是否已退款
     * @param file 账单附件
     */
    void addBill(Integer bookId, Integer inAssetId, Integer outAssetId, Integer payBillId, Integer billCategoryId,
                 BillType type, BigDecimal amount, BigDecimal transferFee, Timestamp time, String remark, Boolean refunded, MultipartFile file);

    /**
     * 获取账本的所有账单
     * @param bookId 账本ID
     * @return 账单列表
     */
    List<BaseBill> getBillByBook(Integer bookId);

    /**
     * 获取账本指定时间范围的账单
     * @param bookId 账本ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 账单列表
     */
    List<BaseBill> getBillByBookTime(Integer bookId, Timestamp startTime, Timestamp endTime);

    /**
     * 获取指定账单
     * @param id 账单ID
     * @param type 账单类型
     * @return 账单实体
     */
    BaseBill getBill(Integer id, BillType type);

    /**
     * 获取分类支出统计
     * @param bookId 账本ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 分类支出统计数据
     */
    List<Map<String, Object>> categoryPayStatisticTime(Integer bookId, Timestamp startTime, Timestamp endTime);

    /**
     * 获取分类收入统计
     * @param bookId 账本ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 分类收入统计数据
     */
    List<Map<String, Object>> categoryIncomeStatisticTime(Integer bookId, Timestamp startTime, Timestamp endTime);

    /**
     * 获取日支出统计
     * @param bookId 账本ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 日支出统计数据
     */
    List<Map<String, Object>> getDayPayStatisticTime(Integer bookId, Timestamp startTime, Timestamp endTime);

    /**
     * 获取日收入统计
     * @param bookId 账本ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 日收入统计数据
     */
    List<Map<String, Object>> getDayIncomeStatisticTime(Integer bookId, Timestamp startTime, Timestamp endTime);

    /**
     * 获取最大最小支出账单
     * @param bookId 账本ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 最大最小支出账单
     */
    Map<String, PayBill> getMaxMinPayBill(Integer bookId, Timestamp startTime, Timestamp endTime);

    /**
     * 获取最大最小收入账单
     * @param bookId 账本ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 最大最小收入账单
     */
    Map<String, IncomeBill> getMaxMinIncomeBill(Integer bookId, Timestamp startTime, Timestamp endTime);

    /**
     * 获取账单图片
     * @param id 账单ID
     * @param type 账单类型
     * @return 图片数据
     */
    BillImageResponse getBillImage(Integer id, BillType type);

    /**
     * 修改账单
     * @param id 账单ID
     * @param bookId 账本ID
     * @param amount 账单金额
     * @param billTime 账单时间
     * @param remark 账单备注
     * @param inAssetId 收入资产ID
     * @param outAssetId 支出资产ID
     * @param billCategoryId 账单分类ID
     * @param refunded 是否已退款
     * @param type 账单类型
     * @param file 账单附件
     * @param payBillId 关联的支出账单ID（用于退款）
     * @param transferFee 转账手续费
     */
    void changeBill(Integer id, Integer bookId, BigDecimal amount, Timestamp billTime, String remark, Integer inAssetId, Integer outAssetId,
                    Integer billCategoryId, Boolean refunded, BillType type, MultipartFile file, Integer payBillId, BigDecimal transferFee);

    /**
     * 删除账单
     * @param id 账单ID
     * @param type 账单类型
     */
    void deleteBill(Integer id, BillType type);

    /**
     * 删除账单图片
     * @param id 账单ID
     * @param type 账单类型
     */
    void deleteBillImage(Integer id, BillType type);
}