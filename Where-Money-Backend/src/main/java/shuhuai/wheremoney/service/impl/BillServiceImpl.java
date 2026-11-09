package shuhuai.wheremoney.service.impl;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import shuhuai.wheremoney.entity.*;
import shuhuai.wheremoney.mapper.IncomeBillMapper;
import shuhuai.wheremoney.mapper.PayBillMapper;
import shuhuai.wheremoney.mapper.RefundBillMapper;
import shuhuai.wheremoney.mapper.TransferBillMapper;
import shuhuai.wheremoney.response.bill.BillImageResponse;
import shuhuai.wheremoney.service.AssetService;
import shuhuai.wheremoney.service.BillCategoryService;
import shuhuai.wheremoney.service.BillService;
import shuhuai.wheremoney.service.BudgetService;
import shuhuai.wheremoney.service.excep.common.ParamsException;
import shuhuai.wheremoney.service.excep.common.ServerException;
import shuhuai.wheremoney.type.BillType;
import shuhuai.wheremoney.utils.RedisConnector;
import shuhuai.wheremoney.utils.TimeComputer;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.*;

/**
 * 账单服务实现类
 * <p>
 * 处理各种账单相关操作，包括添加、获取、修改、删除账单，以及账单统计分析等功能
 * </p>
 */
@Service
public class BillServiceImpl implements BillService {
    @Resource
    private shuhuai.wheremoney.mapper.LedgerLockMapper ledgerLocks;
    /**
     * Redis中账单缓存过期时间（天）
     */
    @Value("${redis.bill.expire}")
    private Long billExpire;

    /**
     * 账单分类服务
     */
    @jakarta.annotation.Resource
    private BillCategoryService billCategoryService;

    /**
     * 资产服务
     */
    @Resource
    private AssetService assetService;

    /**
     * 支出账单Mapper
     */
    @jakarta.annotation.Resource
    private PayBillMapper payBillMapper;

    /**
     * 收入账单Mapper
     */
    @Resource
    private IncomeBillMapper incomeBillMapper;

    /**
     * 转账账单Mapper
     */
    @Resource
    private TransferBillMapper transferBillMapper;

    /**
     * 退款账单Mapper
     */
    @Resource
    private RefundBillMapper refundBillMapper;

    /**
     * 预算服务
     */
    @jakarta.annotation.Resource
    private BudgetService budgetService;

    /**
     * Redis连接器
     */
    @jakarta.annotation.Resource
    private RedisConnector redisConnector;

    /**
     * 将账单写入Redis缓存
     * <p>
     * 注意：写入前会清空账单的图片数据，避免缓存过大
     * </p>
     *
     * @param key 缓存键
     * @param bill 账单对象
     */
    private void writeToRedis(String key, BaseBill bill) {
        if (bill != null) {
            bill.setImage(null);
            redisConnector.writeObject(key, bill, TimeComputer.dayToSecond(billExpire));
        }
    }

    /**
     * 计算指定支出账单的退款总额
     * <p>
     * 可选择排除某个特定的退款记录
     * </p>
     *
     * @param payBillId 支出账单ID
     * @param excludeRefundId 要排除的退款记录ID，可为null
     * @return 退款总金额
     */
    private BigDecimal sumRefundAmount(Integer payBillId, Integer excludeRefundId) {
        BigDecimal total = BigDecimal.ZERO;
        List<RefundBill> refundBills = refundBillMapper.selectRefundBillByPayBillId(payBillId);
        if (refundBills == null) {
            return total;
        }
        for (RefundBill refundBill : refundBills) {
            if (excludeRefundId != null && excludeRefundId.equals(refundBill.getId())) {
                continue;
            }
            total = total.add(refundBill.getAmount());
        }
        return total;
    }

    /**
     * 刷新支出账单的退款状态
     * <p>
     * 根据该支出账单是否有退款记录，更新其 refunded 字段
     * </p>
     *
     * @param payBillId 支出账单ID
     * @throws ServerException 服务器更新失败时抛出
     */
    private void refreshPayBillRefunded(Integer payBillId) {
        List<RefundBill> refundBills = refundBillMapper.selectRefundBillByPayBillId(payBillId);
        boolean refunded = refundBills != null && !refundBills.isEmpty();
        Integer updateResult = payBillMapper.updatePayBillByIdSelective(new PayBill(payBillId, refunded));
        if (updateResult == null || updateResult != 1) {
            throw new ServerException("服务器错误");
        }
        redisConnector.deleteObject("pay_bill:" + payBillId);
        if (redisConnector.existObject("pay_bill:" + payBillId)) {
            PayBill payBill = (PayBill) redisConnector.readObject("pay_bill:" + payBillId);
            if (payBill != null) {
                payBill.setRefunded(refunded);
                writeToRedis("pay_bill:" + payBillId, payBill);
            }
        }
    }

    /**
     * 重建指定账本的预算
     * <p>
     * 去重处理，避免重复重建同一账本的预算
     * </p>
     *
     * @param bookIds 账本ID列表
     */
    private void rebuildBudgetByBooks(Integer... bookIds) {
        Set<Integer> ids = new HashSet<>();
        for (Integer bookId : bookIds) {
            if (bookId != null && ids.add(bookId)) {
                budgetService.rebuildBudgetByBook(bookId);
            }
        }
    }

    /**
     * 验证金额是否为正数
     *
     * @param amount 待验证的金额
     * @throws ParamsException 金额为null或非正数时抛出
     */
    private void validatePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0 || amount.scale() > 2
                || amount.compareTo(new BigDecimal("99999999.99")) > 0) {
            throw new ParamsException("参数错误");
        }
    }

    /**
     * 验证转账金额和手续费
     * <p>
     * 验证转账金额为正数，手续费不为负数
     * </p>
     *
     * @param amount 转账金额
     * @param transferFee 手续费
     * @throws ParamsException 验证失败时抛出
     */
    private void validateTransferAmountAndFee(BigDecimal amount, BigDecimal transferFee) {
        validatePositiveAmount(amount);
        if (transferFee != null && (transferFee.compareTo(BigDecimal.ZERO) < 0 || transferFee.scale() > 2
                || transferFee.compareTo(amount) > 0)) {
            throw new ParamsException("参数错误");
        }
    }

    /**
     * 获取账单在Redis中的缓存键
     *
     * @param type 账单类型
     * @param id 账单ID
     * @return 缓存键字符串
     */
    private String getBillCacheKey(BillType type, Integer id) {
        return switch (type) {
            case 支出 -> "pay_bill:" + id;
            case 收入 -> "income_bill:" + id;
            case 退款 -> "refund_bill:" + id;
            case 转账 -> "transfer_bill:" + id;
        };
    }

    /**
     * 清除账单在Redis中的缓存
     *
     * @param type 账单类型
     * @param id 账单ID
     */
    private void evictBillCache(BillType type, Integer id) {
        String key = getBillCacheKey(type, id);
        redisConnector.deleteObject(key);
    }

    /**
     * 添加账单
     * <p>
     * 根据账单类型添加不同类型的账单，并更新相关资产余额
     * </p>
     *
     * @param bookId 账本ID
     * @param inAssetId 收入资产ID（用于收入、退款、转账）
     * @param outAssetId 支出资产ID（用于支出、转账）
     * @param payBillId 支出账单ID（用于退款）
     * @param billCategoryId 账单分类ID（用于支出、收入）
     * @param type 账单类型
     * @param amount 金额
     * @param transferFee 转账手续费（用于转账）
     * @param time 账单时间
     * @param remark 备注
     * @param refunded 是否已退款（用于支出）
     * @param file 附件文件
     * @throws ParamsException 参数错误时抛出
     * @throws ServerException 服务器错误时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addBill(Integer bookId, Integer inAssetId, Integer outAssetId, Integer payBillId, Integer billCategoryId,
                        BillType type, BigDecimal amount, BigDecimal transferFee, Timestamp time, String remark, Boolean refunded, MultipartFile file) {
        if (bookId == null || type == null || time == null || ledgerLocks.book(bookId) == null) throw new ParamsException("账本或账单参数不正确");
        if (remark != null && remark.length() > 255) throw new ParamsException("备注最多255字");
        var assetIds = java.util.stream.Stream.of(inAssetId, outAssetId).filter(java.util.Objects::nonNull).distinct().sorted().toList();
        if (!assetIds.isEmpty()) ledgerLocks.assets(assetIds);
        if (type == BillType.转账 && java.util.Objects.equals(inAssetId, outAssetId)) throw new ParamsException("转入转出账户不能相同");
        byte[] fileBytes = null;
        String imageContentType = null;
        if (file != null) {
            try {
                imageContentType = file.getContentType(); // image/heic, image/avif, image/jpeg...
                fileBytes = file.getBytes();
            } catch (IOException e) {
                throw new ServerException("服务器错误");
            }
        }
        switch (type) {
            case 支出 -> {
                // 验证支出账单必要参数
                if (bookId == null || outAssetId == null || billCategoryId == null) {
                    throw new ParamsException("参数错误");
                }
                // 验证金额为正数
                validatePositiveAmount(amount);
                // 创建支出账单对象
                PayBill payBill = new PayBill(bookId, outAssetId, billCategoryId, amount, time, remark, false, fileBytes, imageContentType);
                // 减少支出资产余额
                assetService.changeBalanceRelative(outAssetId, amount.negate());
                // 插入支出账单记录
                payBillMapper.insertPayBillSelective(payBill);
                // 写入Redis缓存
                writeToRedis("pay_bill:" + payBill.getId(), payBill);
                // 重建账本预算
                rebuildBudgetByBooks(bookId);
            }
            case 收入 -> {
                // 验证收入账单必要参数
                if (bookId == null || inAssetId == null || billCategoryId == null) {
                    throw new ParamsException("参数错误");
                }
                // 验证金额为正数
                validatePositiveAmount(amount);
                // 增加收入资产余额
                assetService.changeBalanceRelative(inAssetId, amount);
                // 创建收入账单对象
                IncomeBill incomeBill = new IncomeBill(bookId, inAssetId, billCategoryId, amount, time, remark, fileBytes, imageContentType);
                // 插入收入账单记录
                incomeBillMapper.insertIncomeBillSelective(incomeBill);
                // 写入Redis缓存
                writeToRedis("income_bill:" + incomeBill.getId(), incomeBill);
            }
            case 转账 -> {
                // 验证转账账单必要参数
                if (bookId == null || inAssetId == null || outAssetId == null) {
                    throw new ParamsException("参数错误");
                }
                // 验证转账金额和手续费
                validateTransferAmountAndFee(amount, transferFee);
                // 增加入账资产余额（扣除手续费）
                assetService.changeBalanceRelative(inAssetId, amount.subtract(transferFee != null ? transferFee : BigDecimal.ZERO));
                // 减少出账资产余额
                assetService.changeBalanceRelative(outAssetId, amount.negate());
                // 创建转账账单对象
                TransferBill transferBill = new TransferBill(bookId, inAssetId, outAssetId, amount,
                        transferFee == null ? BigDecimal.ZERO : transferFee, time, remark, fileBytes, imageContentType);
                // 插入转账账单记录
                transferBillMapper.insertTransferBillSelective(transferBill);
                // 写入Redis缓存
                writeToRedis("transfer_bill:" + transferBill.getId(), transferBill);
            }
            case 退款 -> {
                // 验证退款账单必要参数
                if (bookId == null || amount == null || payBillId == null || inAssetId == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new ParamsException("参数错误");
                }
                // 锁定支出账单，防止并发修改
                PayBill payBill = payBillMapper.selectPayBillByIdForUpdate(payBillId);
                // 验证支出账单存在且属于同一账本
                if (payBill == null || !Objects.equals(payBill.getBookId(), bookId)) {
                    throw new ParamsException("参数错误");
                }
                // 计算已退款金额
                BigDecimal refundedAmount = sumRefundAmount(payBillId, null);
                // 计算可退款余额
                BigDecimal remainAmount = payBill.getAmount().subtract(refundedAmount);
                // 验证退款金额不超过可退余额
                if (remainAmount.compareTo(BigDecimal.ZERO) <= 0 || amount.compareTo(remainAmount) > 0) {
                    throw new ParamsException("退款金额超过可退金额");
                }
                // 创建退款账单对象
                RefundBill refundBill = new RefundBill(bookId, payBillId, inAssetId, amount, time, remark, fileBytes, imageContentType);
                // 增加退款资产余额
                assetService.changeBalanceRelative(inAssetId, amount);
                // 插入退款账单记录
                refundBillMapper.insertRefundBillSelective(refundBill);
                // 刷新支出账单的退款状态
                refreshPayBillRefunded(payBillId);
                // 写入Redis缓存
                writeToRedis("refund_bill:" + refundBill.getId(), refundBill);
                // 重建账本预算
                rebuildBudgetByBooks(bookId);
            }
        }
    }

    /**
     * 获取指定账本的所有账单
     * <p>
     * 包括支出、收入、转账和退款四种类型的账单，按时间倒序排列
     * </p>
     *
     * @param bookId 账本ID
     * @return 账单列表
     * @throws ParamsException 参数错误时抛出
     */
    @Override
    public List<BaseBill> getBillByBook(Integer bookId) {
        if (bookId == null) {
            throw new ParamsException("参数错误");
        }
        List<BaseBill> bills = new ArrayList<>();
        bills.addAll(payBillMapper.selectPayBillByBookId(bookId));
        bills.addAll(incomeBillMapper.selectIncomeBillByBookId(bookId));
        bills.addAll(transferBillMapper.selectTransferBillByBookId(bookId));
        bills.addAll(refundBillMapper.selectRefundBillByBookId(bookId));
        bills.sort(Comparator.comparing(BaseBill::getBillTime).reversed());
        return bills;
    }

    /**
     * 获取指定账本在指定时间范围内的所有账单
     * <p>
     * 包括支出、收入、转账和退款四种类型的账单，按时间倒序排列
     * </p>
     *
     * @param bookId 账本ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 账单列表
     * @throws ParamsException 参数错误时抛出
     */
    @Override
    public List<BaseBill> getBillByBookTime(Integer bookId, Timestamp startTime, Timestamp endTime) {
        if (bookId == null || startTime == null || endTime == null) {
            throw new ParamsException("参数错误");
        }
        List<BaseBill> bills = new ArrayList<>();
        bills.addAll(payBillMapper.selectPayBillByBookIdTime(bookId, startTime, endTime));
        bills.addAll(incomeBillMapper.selectIncomeBillByBookIdTime(bookId, startTime, endTime));
        bills.addAll(transferBillMapper.selectTransferBillByBookIdTime(bookId, startTime, endTime));
        bills.addAll(refundBillMapper.selectRefundBillByBookIdTime(bookId, startTime, endTime));
        bills.sort(Comparator.comparing(BaseBill::getBillTime).reversed());
        return bills;
    }

    /**
     * 获取指定类型的账单
     * <p>
     * 优先从Redis缓存获取，缓存未命中则从数据库查询并写入缓存
     * </p>
     *
     * @param id 账单ID
     * @param type 账单类型
     * @return 账单对象
     * @throws ParamsException 参数错误时抛出
     */
    @Override
    public BaseBill getBill(Integer id, BillType type) {
        if (id == null || type == null) {
            throw new ParamsException("参数错误");
        }
        Object cached = redisConnector.readObject(getBillCacheKey(type, id));
        if (cached instanceof BaseBill bill) return bill;
        switch (type) {
            case 支出 -> {
                PayBill payBill = org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive()
                    ? payBillMapper.selectPayBillByIdForUpdate(id) : payBillMapper.selectPayBillById(id);
                writeToRedis("pay_bill:" + id, payBill);
                return payBill;
            }
            case 收入 -> {
                IncomeBill incomeBill = org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive()
                    ? incomeBillMapper.selectIncomeBillByIdForUpdate(id) : incomeBillMapper.selectIncomeBillById(id);
                writeToRedis("income_bill:" + id, incomeBill);
                return incomeBill;
            }
            case 转账 -> {
                TransferBill transferBill = org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive()
                    ? transferBillMapper.selectTransferBillByIdForUpdate(id) : transferBillMapper.selectTransferBillById(id);
                writeToRedis("transfer_bill:" + id, transferBill);
                return transferBill;
            }
            case 退款 -> {
                RefundBill refundBill = org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive()
                    ? refundBillMapper.selectRefundBillByIdForUpdate(id) : refundBillMapper.selectRefundBillById(id);
                writeToRedis("refund_bill:" + id, refundBill);
                return refundBill;
            }
            default -> throw new ParamsException("参数错误");
        }
    }

    /**
     * 统计退款金额
     * <p>
     * 按支出账单ID分组，计算每个支出账单的总退款金额
     * </p>
     *
     * @param refundBills 退款账单列表
     * @return 支出账单ID到退款总金额的映射
     */
    private Map<Integer, BigDecimal> statisticRefund(List<RefundBill> refundBills) {
        Map<Integer, BigDecimal> result = new HashMap<>();
        for (RefundBill refundBill : refundBills) {
            if (result.containsKey(refundBill.getPayBillId())) {
                result.replace(refundBill.getPayBillId(), result.get(refundBill.getPayBillId()).add(refundBill.getAmount()));
            } else {
                result.put(refundBill.getPayBillId(), refundBill.getAmount());
            }
        }
        return result;
    }

    /**
     * 统计指定时间范围内的支出分类
     * <p>
     * 按分类汇总支出金额，并计算每个分类占总支出的百分比，按金额降序排列
     * </p>
     *
     * @param bookId 账本ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 分类统计结果列表，每个元素包含分类名称、金额和百分比
     * @throws ParamsException 参数错误时抛出
     */
    @Override
    public List<Map<String, Object>> categoryPayStatisticTime(Integer bookId, Timestamp startTime, Timestamp endTime) {
        if (bookId == null || startTime == null || endTime == null) {
            throw new ParamsException("参数错误");
        }
        List<PayBill> payBills = payBillMapper.selectPayBillByBookIdTime(bookId, startTime, endTime);
        List<RefundBill> refundBills = refundBillMapper.selectRefundBillByBookIdTime(bookId, startTime, endTime);
        Map<Integer, BigDecimal> refundMap = statisticRefund(refundBills);
        Map<Integer, BigDecimal> temp = new java.util.HashMap<>();
        BigDecimal total = BigDecimal.ZERO;
        for (PayBill payBill : payBills) {
            Integer categoryId = payBill.getBillCategoryId();
            BigDecimal amount = payBill.getAmount();
            if (refundMap.containsKey(payBill.getId())) {
                amount = amount.subtract(refundMap.get(payBill.getId()));
            }
            if (temp.containsKey(categoryId)) {
                temp.replace(categoryId, temp.get(categoryId).add(amount));
            } else {
                temp.put(categoryId, amount);
            }
            total = total.add(amount);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Integer, BigDecimal> entry : temp.entrySet()) {
            if (entry.getValue().compareTo(BigDecimal.ZERO) > 0) {
                result.add(Map.of("category", billCategoryService.getBillCategory(entry.getKey()).getBillCategoryName(), "amount", entry.getValue(),
                        "percent", entry.getValue().divide(total, 4, RoundingMode.HALF_UP).movePointRight(2) + "%"));
            }
        }
        result.sort((first, second) -> ((BigDecimal) second.get("amount")).compareTo((BigDecimal) first.get("amount")));
        return result;
    }

    /**
     * 统计指定时间范围内的收入分类
     * <p>
     * 按分类汇总收入金额，并计算每个分类占总收入的百分比，按金额降序排列
     * </p>
     *
     * @param bookId 账本ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 分类统计结果列表，每个元素包含分类名称、金额和百分比
     * @throws ParamsException 参数错误时抛出
     */
    @Override
    public List<Map<String, Object>> categoryIncomeStatisticTime(Integer bookId, Timestamp startTime, Timestamp endTime) {
        if (bookId == null || startTime == null || endTime == null) {
            throw new ParamsException("参数错误");
        }
        List<IncomeBill> incomeBills = incomeBillMapper.selectIncomeBillByBookIdTime(bookId, startTime, endTime);
        Map<Integer, BigDecimal> temp = new java.util.HashMap<>();
        BigDecimal total = BigDecimal.ZERO;
        for (IncomeBill incomeBill : incomeBills) {
            Integer categoryId = incomeBill.getBillCategoryId();
            BigDecimal amount = incomeBill.getAmount();
            if (temp.containsKey(categoryId)) {
                temp.replace(categoryId, temp.get(categoryId).add(amount));
            } else {
                temp.put(categoryId, amount);
            }
            total = total.add(amount);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Integer, BigDecimal> entry : temp.entrySet()) {
            if (entry.getValue().compareTo(BigDecimal.ZERO) > 0) {
                result.add(Map.of("category", billCategoryService.getBillCategory(entry.getKey()).getBillCategoryName(), "amount", entry.getValue(),
                        "percent", entry.getValue().divide(total, 4, RoundingMode.HALF_UP).movePointRight(2) + "%"));
            }
        }
        result.sort((first, second) -> ((BigDecimal) second.get("amount")).compareTo((BigDecimal) first.get("amount")));
        return result;
    }

    /**
     * 统计指定时间范围内的每日支出
     * <p>
     * 按日期汇总支出金额，计算每天的总支出
     * </p>
     *
     * @param bookId 账本ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 每日支出统计结果列表，每个元素包含日期和金额
     * @throws ParamsException 参数错误时抛出
     */
    @Override
    public List<Map<String, Object>> getDayPayStatisticTime(Integer bookId, Timestamp startTime, Timestamp endTime) {
        if (bookId == null || startTime == null || endTime == null) {
            throw new ParamsException("参数错误");
        }
        List<Map<String, Object>> result = new ArrayList<>();
        List<PayBill> payBills = payBillMapper.selectPayBillByBookIdTime(bookId, startTime, endTime);
        List<RefundBill> refundBills = refundBillMapper.selectRefundBillByBookIdTime(bookId, startTime, endTime);
        Map<Integer, BigDecimal> refundMap = statisticRefund(refundBills);
        // 计算退款
        for (PayBill payBill : payBills) {
            if (refundMap.containsKey(payBill.getId())) {
                payBill.setAmount(payBill.getAmount().subtract(refundMap.get(payBill.getId())));
            }
        }
        // 计算每日支出
        for (Timestamp time = TimeComputer.getDay(startTime); time.before(endTime); time = TimeComputer.nextDay(time)) {
            Timestamp temp = time;
            result.add(Map.of("day", temp, "amount", payBills.stream().filter(bill -> bill.getBillTime().equals(temp) ||
                            bill.getBillTime().after(temp) && bill.getBillTime().before(TimeComputer.nextDay(temp)))
                    .map(PayBill::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add)));
        }
        return result;
    }

    /**
     * 统计指定时间范围内的每日收入
     * <p>
     * 按日期汇总收入金额，计算每天的总收入
     * </p>
     *
     * @param bookId 账本ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 每日收入统计结果列表，每个元素包含日期和金额
     * @throws ParamsException 参数错误时抛出
     */
    @Override
    public List<Map<String, Object>> getDayIncomeStatisticTime(Integer bookId, Timestamp startTime, Timestamp endTime) {
        if (bookId == null || startTime == null || endTime == null) {
            throw new ParamsException("参数错误");
        }
        List<Map<String, Object>> result = new ArrayList<>();
        List<IncomeBill> incomeBills = incomeBillMapper.selectIncomeBillByBookIdTime(bookId, startTime, endTime);
        // 计算每日收入
        for (Timestamp time = TimeComputer.getDay(startTime); time.before(endTime); time = TimeComputer.nextDay(time)) {
            Timestamp temp = time;
            result.add(Map.of("day", temp, "amount", incomeBills.stream().filter(bill -> bill.getBillTime().equals(temp) ||
                            bill.getBillTime().after(temp) && bill.getBillTime().before(TimeComputer.nextDay(temp)))
                    .map(IncomeBill::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add)));
        }
        return result;
    }

    /**
     * 获取指定时间范围内的最大和最小支出账单
     * <p>
     * 考虑退款因素，计算实际支出金额后再比较
     * </p>
     *
     * @param bookId 账本ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 包含最大和最小支出账单的映射
     * @throws ParamsException 参数错误时抛出
     */
    @Override
    public Map<String, PayBill> getMaxMinPayBill(Integer bookId, Timestamp startTime, Timestamp endTime) {
        if (bookId == null || startTime == null || endTime == null) {
            throw new ParamsException("参数错误");
        }
        List<PayBill> payBills = payBillMapper.selectPayBillByBookIdTime(bookId, startTime, endTime);
        // 如果没有支出账单，返回默认值
        if (payBills.isEmpty()) {
            return new HashMap<>(Map.of(
                    "max", new PayBill(null, null, null, null, BigDecimal.ZERO, null, null, null, null, null),
                    "min", new PayBill(null, null, null, null, BigDecimal.ZERO, null, null, null, null, null)));
        }
        PayBill max = null;
        PayBill min = null;
        // 考虑退款因素
        for (PayBill payBill : payBills) {
            // 如果已退款，计算退款金额
            if (payBill.getRefunded()) {
                List<RefundBill> refundBills = refundBillMapper.selectRefundBillByPayBillId(payBill.getId());
                BigDecimal refundAmount = refundBills.stream().map(RefundBill::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                payBill.setAmount(payBill.getAmount().subtract(refundAmount));
            }
            if (payBill.getAmount().compareTo(BigDecimal.ZERO) > 0 && (max == null || payBill.getAmount().compareTo(max.getAmount()) > 0)) {
                max = payBill;
            }
            if (payBill.getAmount().compareTo(BigDecimal.ZERO) > 0 && (min == null || payBill.getAmount().compareTo(min.getAmount()) < 0)) {
                min = payBill;
            }
        }
        return new HashMap<>(Map.of("max", max == null ? payBills.getFirst() : max, "min", min == null ? payBills.getFirst() : min));
    }

    /**
     * 获取指定时间范围内的最大和最小收入账单
     *
     * @param bookId 账本ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 包含最大和最小收入账单的映射
     * @throws ParamsException 参数错误时抛出
     */
    @Override
    public Map<String, IncomeBill> getMaxMinIncomeBill(Integer bookId, Timestamp startTime, Timestamp endTime) {
        if (bookId == null || startTime == null || endTime == null) {
            throw new ParamsException("参数错误");
        }
        List<IncomeBill> incomeBills = incomeBillMapper.selectIncomeBillByBookIdTime(bookId, startTime, endTime);
        if (incomeBills.isEmpty()) {
            return new HashMap<>(Map.of(
                    "max", new IncomeBill(null, null, null, null, BigDecimal.ZERO, null, null, null, null),
                    "min", new IncomeBill(null, null, null, null, BigDecimal.ZERO, null, null, null, null)));
        }
        IncomeBill max = null;
        IncomeBill min = null;
        for (IncomeBill incomeBill : incomeBills) {
            if (max == null || incomeBill.getAmount().compareTo(max.getAmount()) > 0) {
                max = incomeBill;
            }
            if (min == null || incomeBill.getAmount().compareTo(min.getAmount()) < 0) {
                min = incomeBill;
            }
        }
        return new HashMap<>(Map.of("max", max == null ? incomeBills.getFirst() : max, "min", min == null ? incomeBills.getFirst() : min));
    }

    /**
     * 获取账单的图片
     *
     * @param id 账单ID
     * @param type 账单类型
     * @return 图片字节数组
     * @throws ParamsException 参数错误时抛出
     */
    @Override
    public BillImageResponse getBillImage(Integer id, BillType type) {
        if (id == null || type == null) {
            throw new ParamsException("参数错误");
        }
        switch (type) {
            case 支出 -> {
                PayBill payBill = payBillMapper.selectPayBillById(id);
                if (payBill == null) {
                    throw new ParamsException("参数错误");
                }
                return new BillImageResponse(payBill.getImageContentType(), payBill.getImage());
            }
            case 收入 -> {
                IncomeBill incomeBill = incomeBillMapper.selectIncomeBillById(id);
                if (incomeBill == null) {
                    throw new ParamsException("参数错误");
                }
                return new BillImageResponse(incomeBill.getImageContentType(), incomeBill.getImage());
            }
            case 退款 -> {
                RefundBill refundBill = refundBillMapper.selectRefundBillById(id);
                if (refundBill == null) {
                    throw new ParamsException("参数错误");
                }
                return new BillImageResponse(refundBill.getImageContentType(), refundBill.getImage());
            }
            case 转账 -> {
                TransferBill transferBill = transferBillMapper.selectTransferBillById(id);
                if (transferBill == null) {
                    throw new ParamsException("参数错误");
                }
                return new BillImageResponse(transferBill.getImageContentType(), transferBill.getImage());
            }
            default -> throw new ParamsException("参数错误");
        }
    }


    /**
     * 修改账单
     * <p>
     * 根据账单类型修改不同类型的账单，并更新相关资产余额
     * </p>
     *
     * @param id 账单ID
     * @param bookId 账本ID
     * @param amount 金额
     * @param billTime 账单时间
     * @param remark 备注
     * @param inAssetId 收入资产ID（用于收入、退款、转账）
     * @param outAssetId 支出资产ID（用于支出、转账）
     * @param billCategoryId 账单分类ID（用于支出、收入）
     * @param refunded 是否已退款（用于支出）
     * @param type 账单类型
     * @param file 附件文件
     * @param payBillId 支出账单ID（用于退款）
     * @param transferFee 转账手续费（用于转账）
     * @throws ParamsException 参数错误时抛出
     * @throws ServerException 服务器错误时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeBill(Integer id, Integer bookId, BigDecimal amount, Timestamp billTime, String remark, Integer inAssetId, Integer outAssetId,
                           Integer billCategoryId, Boolean refunded, BillType type, MultipartFile file, Integer payBillId, BigDecimal transferFee) {
        if (id == null || type == null) {
            throw new ParamsException("参数错误");
        }
        byte[] fileBytes = null;
        String fileContentType = null;
        if (file != null) {
            try {
                fileBytes = file.getBytes();
                fileContentType = file.getContentType();
            } catch (IOException e) {
                throw new ServerException("服务器错误");
            }
        }
        switch (type) {
            case 支出 -> {
                // 获取原支出账单
                PayBill originBill = (PayBill) getBill(id, BillType.支出);
                if (originBill == null) {
                    throw new ParamsException("参数错误");
                }
                // 记录原账本ID
                Integer originBookId = originBill.getBookId();
                // 创建新支出账单对象
                PayBill newBill = new PayBill(id, bookId, outAssetId, billCategoryId, amount, billTime, remark, refunded, fileBytes, fileContentType);
                // 确定当前账本ID
                Integer currentBookId = newBill.getBookId() == null ? originBookId : newBill.getBookId();
                // 确定目标金额
                BigDecimal targetAmount = newBill.getAmount() == null ? originBill.getAmount() : newBill.getAmount();
                // 验证金额为正数
                validatePositiveAmount(targetAmount);
                // 获取关联的退款账单
                List<RefundBill> refundBills = refundBillMapper.selectRefundBillByPayBillId(originBill.getId());
                if (refundBills == null) {
                    refundBills = new ArrayList<>();
                }
                // 处理退款状态变更
                if (newBill.getRefunded() != null && originBill.getRefunded() && !newBill.getRefunded()) {
                    // 如果从已退款变为未退款，删除所有关联的退款账单
                    for (RefundBill refundBill : refundBills) {
                        deleteBill(refundBill);
                    }
                } else {
                    // 计算已退款金额
                    BigDecimal refundedAmount = refundBills.stream().map(RefundBill::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    // 验证退款总额不超过支出金额
                    if (refundedAmount.compareTo(targetAmount) > 0) {
                        throw new ParamsException("退款总额超过支出金额");
                    }
                    // 处理账本变更
                    if (!Objects.equals(currentBookId, originBookId) && !refundBills.isEmpty()) {
                        // 如果账本变更，更新所有关联退款账单的账本ID
                        for (RefundBill refundBill : refundBills) {
                            Integer updateRefundResult = refundBillMapper.updateRefundBillByIdSelective(
                                    new RefundBill(refundBill.getId(), currentBookId, null, null, null, null, null, null));
                            if (updateRefundResult == null || updateRefundResult != 1) {
                                throw new ServerException("服务器错误");
                            }
                            // 更新Redis缓存
                            if (redisConnector.existObject("refund_bill:" + refundBill.getId())) {
                                RefundBill cacheRefundBill = (RefundBill) redisConnector.readObject("refund_bill:" + refundBill.getId());
                                if (cacheRefundBill != null) {
                                    cacheRefundBill.setBookId(currentBookId);
                                    writeToRedis("refund_bill:" + refundBill.getId(), cacheRefundBill);
                                }
                            }
                        }
                    }
                }
                // 处理资产变更
                if (newBill.getPayAssetId() != null && !Objects.equals(newBill.getPayAssetId(), originBill.getPayAssetId())) {
                    // 如果支付资产变更，先回滚原资产余额
                    assetService.changeBalanceRelative(originBill.getPayAssetId(), originBill.getAmount());
                    // 再更新新资产余额
                    if (newBill.getAmount() != null && originBill.getAmount().compareTo(newBill.getAmount()) != 0) {
                        assetService.changeBalanceRelative(newBill.getPayAssetId(), newBill.getAmount().negate());
                    } else {
                        assetService.changeBalanceRelative(newBill.getPayAssetId(), originBill.getAmount().negate());
                    }
                } else {
                    // 如果资产不变，只更新金额变更
                    if (newBill.getAmount() != null && originBill.getAmount().compareTo(newBill.getAmount()) != 0) {
                        assetService.changeBalanceRelative(originBill.getPayAssetId(), originBill.getAmount().subtract(newBill.getAmount()));
                    }
                }
                // 更新支出账单
                Integer result = payBillMapper.updatePayBillByIdSelective(newBill);
                if (result != 1) {
                    throw new ServerException("服务器错误");
                }
                // 重建账本预算
                rebuildBudgetByBooks(originBookId, currentBookId);
            }
            case 收入 -> {
                // 获取原收入账单
                IncomeBill originBill = (IncomeBill) getBill(id, BillType.收入);
                if (originBill == null) {
                    throw new ParamsException("参数错误");
                }
                // 创建新收入账单对象
                IncomeBill newBill = new IncomeBill(id, bookId, inAssetId, billCategoryId, amount, billTime, remark, fileBytes, fileContentType);
                // 确定目标金额
                BigDecimal targetAmount = newBill.getAmount() == null ? originBill.getAmount() : newBill.getAmount();
                // 验证金额为正数
                validatePositiveAmount(targetAmount);
                // 处理资产变更
                if (newBill.getIncomeAssetId() != null && !Objects.equals(newBill.getIncomeAssetId(), originBill.getIncomeAssetId())) {
                    // 如果收入资产变更，先回滚原资产余额
                    assetService.changeBalanceRelative(originBill.getIncomeAssetId(), originBill.getAmount().negate());
                    // 再更新新资产余额
                    if (newBill.getAmount() != null && originBill.getAmount().compareTo(newBill.getAmount()) != 0) {
                        assetService.changeBalanceRelative(newBill.getIncomeAssetId(), newBill.getAmount());
                    } else {
                        assetService.changeBalanceRelative(newBill.getIncomeAssetId(), originBill.getAmount());
                    }
                } else {
                    // 如果资产不变，只更新金额变更
                    if (newBill.getAmount() != null && originBill.getAmount().compareTo(newBill.getAmount()) != 0) {
                        assetService.changeBalanceRelative(originBill.getIncomeAssetId(), originBill.getAmount().subtract(newBill.getAmount()).negate());
                    }
                }
                // 更新收入账单
                Integer result = incomeBillMapper.updateIncomeBillByIdSelective(newBill);
                if (result != 1) {
                    throw new ServerException("服务器错误");
                }
            }
            case 退款 -> {
                // 获取原退款账单
                RefundBill originBill = (RefundBill) getBill(id, BillType.退款);
                if (originBill == null) {
                    throw new ParamsException("参数错误");
                }
                // 记录原账本ID
                Integer originBookId = originBill.getBookId();
                // 创建新退款账单对象
                RefundBill newBill = new RefundBill(id, bookId, payBillId, inAssetId, amount, billTime, remark, fileBytes, fileContentType);
                // 验证支出账单ID不变
                if (newBill.getPayBillId() != null && !Objects.equals(newBill.getPayBillId(), originBill.getPayBillId())) {
                    throw new ParamsException("参数错误");
                }
                // 锁定关联的支出账单
                PayBill payBill = payBillMapper.selectPayBillByIdForUpdate(originBill.getPayBillId());
                if (payBill == null) {
                    throw new ParamsException("参数错误");
                }
                // 确定当前账本ID
                Integer currentBookId = newBill.getBookId() == null ? originBookId : newBill.getBookId();
                // 验证退款账单和支出账单属于同一账本
                if (!Objects.equals(currentBookId, payBill.getBookId())) {
                    throw new ParamsException("参数错误");
                }
                // 确定目标金额
                BigDecimal targetAmount = newBill.getAmount() == null ? originBill.getAmount() : newBill.getAmount();
                // 验证金额为正数
                if (targetAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new ParamsException("参数错误");
                }
                // 计算除当前退款外的已退款金额
                BigDecimal refundedAmountWithoutCurrent = sumRefundAmount(originBill.getPayBillId(), originBill.getId());
                // 计算可退款余额
                BigDecimal remainAmount = payBill.getAmount().subtract(refundedAmountWithoutCurrent);
                // 验证退款金额不超过可退余额
                if (remainAmount.compareTo(BigDecimal.ZERO) <= 0 || targetAmount.compareTo(remainAmount) > 0) {
                    throw new ParamsException("退款金额超过可退金额");
                }
                // 处理资产变更
                if (newBill.getRefundAssetId() != null && !Objects.equals(newBill.getRefundAssetId(), originBill.getRefundAssetId())) {
                    // 如果退款资产变更，先回滚原资产余额
                    assetService.changeBalanceRelative(originBill.getRefundAssetId(), originBill.getAmount().negate());
                    // 再更新新资产余额
                    if (newBill.getAmount() != null && originBill.getAmount().compareTo(newBill.getAmount()) != 0) {
                        assetService.changeBalanceRelative(newBill.getRefundAssetId(), newBill.getAmount());
                    } else {
                        assetService.changeBalanceRelative(newBill.getRefundAssetId(), originBill.getAmount());
                    }
                } else {
                    // 如果资产不变，只更新金额变更
                    if (newBill.getAmount() != null && originBill.getAmount().compareTo(newBill.getAmount()) != 0) {
                        assetService.changeBalanceRelative(originBill.getRefundAssetId(), originBill.getAmount().subtract(newBill.getAmount()).negate());
                    }
                }
                // 更新退款账单
                Integer result = refundBillMapper.updateRefundBillByIdSelective(newBill);
                if (result != 1) {
                    throw new ServerException("服务器错误");
                }
                // 刷新支出账单的退款状态
                refreshPayBillRefunded(originBill.getPayBillId());
                // 重建账本预算
                rebuildBudgetByBooks(originBookId, currentBookId, payBill.getBookId());
            }
            case 转账 -> {
                // 获取原转账账单
                TransferBill originBill = (TransferBill) getBill(id, BillType.转账);
                if (originBill == null) {
                    throw new ParamsException("参数错误");
                }
                // 创建新转账账单对象
                TransferBill newBill = new TransferBill(id, bookId, inAssetId, outAssetId, amount, transferFee, billTime, remark, fileBytes, fileContentType);
                // 确定目标入账资产ID
                Integer targetInAssetId = newBill.getInAssetId() == null ? originBill.getInAssetId() : newBill.getInAssetId();
                // 确定目标出账资产ID
                Integer targetOutAssetId = newBill.getOutAssetId() == null ? originBill.getOutAssetId() : newBill.getOutAssetId();
                // 确定目标金额
                BigDecimal targetAmount = newBill.getAmount() == null ? originBill.getAmount() : newBill.getAmount();
                // 确定原手续费
                BigDecimal originTransferFee = originBill.getTransferFee() != null ? originBill.getTransferFee() : BigDecimal.ZERO;
                // 确定目标手续费
                BigDecimal targetTransferFee = newBill.getTransferFee() != null ? newBill.getTransferFee() : originTransferFee;
                // 验证原转账账单的必要字段
                if (originBill.getInAssetId() == null || originBill.getOutAssetId() == null || originBill.getAmount() == null) {
                    throw new ParamsException("参数错误");
                }
                // 验证转账金额和手续费
                validateTransferAmountAndFee(targetAmount, targetTransferFee);
                // 统一计算：先回滚旧转账影响，再应用新转账影响，避免只改入账或只改出账时遗漏迁移。
                // 回滚原出账资产余额
                assetService.changeBalanceRelative(originBill.getOutAssetId(), originBill.getAmount());
                // 回滚原入账资产余额
                assetService.changeBalanceRelative(originBill.getInAssetId(), originBill.getAmount().subtract(originTransferFee).negate());
                // 应用新出账资产余额
                assetService.changeBalanceRelative(targetOutAssetId, targetAmount.negate());
                // 应用新入账资产余额
                assetService.changeBalanceRelative(targetInAssetId, targetAmount.subtract(targetTransferFee));
                // 更新转账账单
                Integer result = transferBillMapper.updateTransferBillByIdSelective(newBill);
                if (result != 1) {
                    throw new ServerException("服务器错误");
                }
            }
        }
        // 清除账单缓存
        evictBillCache(type, id);
    }

    /**
     * 删除账单
     * <p>
     * 根据账单类型删除不同类型的账单，并回滚相关资产余额
     * </p>
     *
     * @param bill 账单对象
     * @throws ServerException 删除失败时抛出
     */
    private void deleteBill(BaseBill bill) {
        Integer result = 0;
        if (bill instanceof PayBill payBill) {
            // 回滚支出资产余额
            assetService.changeBalanceRelative(payBill.getPayAssetId(), payBill.getAmount());
            // 获取关联的退款账单
            List<RefundBill> refundBills = refundBillMapper.selectRefundBillByPayBillId(payBill.getId());
            // 递归删除所有关联的退款账单
            if (refundBills != null && !refundBills.isEmpty()) {
                for (RefundBill refundBill : refundBills) {
                    deleteBill(refundBill);
                }
            }
            // 删除支出账单记录
            result = payBillMapper.deletePayBillById(payBill.getId());
            // 清除Redis缓存
            redisConnector.deleteObject("pay_bill:" + payBill.getId());
        } else if (bill instanceof IncomeBill incomeBill) {
            // 回滚收入资产余额
            assetService.changeBalanceRelative(incomeBill.getIncomeAssetId(), incomeBill.getAmount().negate());
            // 删除收入账单记录
            result = incomeBillMapper.deleteIncomeBillById(incomeBill.getId());
            // 清除Redis缓存
            redisConnector.deleteObject("income_bill:" + incomeBill.getId());
        } else if (bill instanceof TransferBill transferBill) {
            // 回滚出账资产余额
            assetService.changeBalanceRelative(transferBill.getOutAssetId(), transferBill.getAmount());
            // 回滚入账资产余额（扣除手续费）
            assetService.changeBalanceRelative(transferBill.getInAssetId(), transferBill.getAmount().subtract(transferBill.getTransferFee()).negate());
            // 删除转账账单记录
            result = transferBillMapper.deleteTransferBillById(transferBill.getId());
            // 清除Redis缓存
            redisConnector.deleteObject("transfer_bill:" + transferBill.getId());
        } else if (bill instanceof RefundBill refundBill) {
            // 回滚退款资产余额
            assetService.changeBalanceRelative(refundBill.getRefundAssetId(), refundBill.getAmount().negate());
            // 删除退款账单记录
            result = refundBillMapper.deleteRefundBillById(refundBill.getId());
            // 清除Redis缓存
            redisConnector.deleteObject("refund_bill:" + refundBill.getId());
            // 刷新关联支出账单的退款状态
            refreshPayBillRefunded(refundBill.getPayBillId());
        }
        // 验证删除结果
        if (result != 1) {
            throw new ServerException("服务器错误");
        }
    }


    /**
     * 删除账单
     * <p>
     * 根据账单ID和类型删除指定账单，并更新相关预算
     * </p>
     *
     * @param id 账单ID
     * @param type 账单类型
     * @throws ParamsException 参数错误时抛出
     * @throws ServerException 服务器错误时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBill(Integer id, BillType type) {
        if (id == null || type == null) {
            throw new ParamsException("参数错误");
        }
        switch (type) {
            case 支出 -> {
                PayBill payBill = (PayBill) getBill(id, type);
                if (payBill == null) {
                    throw new ParamsException("参数错误");
                }
                Integer bookId = payBill.getBookId();
                deleteBill(payBill);
                rebuildBudgetByBooks(bookId);
            }
            case 收入 -> {
                IncomeBill incomeBill = (IncomeBill) getBill(id, type);
                if (incomeBill == null) {
                    throw new ParamsException("参数错误");
                }
                deleteBill(incomeBill);
            }
            case 退款 -> {
                RefundBill refundBill = (RefundBill) getBill(id, type);
                if (refundBill == null) {
                    throw new ParamsException("参数错误");
                }
                Integer bookId = refundBill.getBookId();
                deleteBill(refundBill);
                rebuildBudgetByBooks(bookId);
            }
            case 转账 -> {
                TransferBill transferBill = (TransferBill) getBill(id, type);
                if (transferBill == null) {
                    throw new ParamsException("参数错误");
                }
                deleteBill(transferBill);
            }
        }
    }

    /**
     * 删除账单图片
     * <p>
     * 根据账单ID和类型删除指定账单的图片
     * </p>
     *
     * @param id 账单ID
     * @param type 账单类型
     * @throws ParamsException 参数错误时抛出
     * @throws ServerException 服务器错误时抛出
     */
    @Override
    public void deleteBillImage(Integer id, BillType type) {
        if (id == null || type == null) {
            throw new ParamsException("参数错误");
        }
        Integer result = 0;
        switch (type) {
            case 支出 -> {
                PayBill payBill = (PayBill) getBill(id, type);
                if (payBill == null) {
                    throw new ParamsException("参数错误");
                }
                payBill.setImage(null);
                payBill.setImageContentType(null);
                result = payBillMapper.updatePayBillById(payBill);
            }
            case 收入 -> {
                IncomeBill incomeBill = (IncomeBill) getBill(id, type);
                if (incomeBill == null) {
                    throw new ParamsException("参数错误");
                }
                incomeBill.setImage(null);
                incomeBill.setImageContentType(null);
                result = incomeBillMapper.updateIncomeBillById(incomeBill);
            }
            case 退款 -> {
                RefundBill refundBill = (RefundBill) getBill(id, type);
                if (refundBill == null) {
                    throw new ParamsException("参数错误");
                }
                refundBill.setImage(null);
                refundBill.setImageContentType(null);
                result = refundBillMapper.updateRefundBillById(refundBill);
            }
            case 转账 -> {
                TransferBill transferBill = (TransferBill) getBill(id, type);
                if (transferBill == null) {
                    throw new ParamsException("参数错误");
                }
                transferBill.setImage(null);
                transferBill.setImageContentType(null);
                result = transferBillMapper.updateTransferBillById(transferBill);
            }
        }
        if (result != 1) {
            throw new ServerException("服务器错误");
        }
    }
}
