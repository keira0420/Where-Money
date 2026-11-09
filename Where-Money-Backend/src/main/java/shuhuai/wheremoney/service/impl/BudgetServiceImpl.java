package shuhuai.wheremoney.service.impl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shuhuai.wheremoney.entity.Book;
import shuhuai.wheremoney.entity.Budget;
import shuhuai.wheremoney.entity.PayBill;
import shuhuai.wheremoney.entity.RefundBill;
import shuhuai.wheremoney.mapper.BookMapper;
import shuhuai.wheremoney.mapper.BudgetMapper;
import shuhuai.wheremoney.mapper.PayBillMapper;
import shuhuai.wheremoney.mapper.RefundBillMapper;
import shuhuai.wheremoney.service.BudgetService;
import shuhuai.wheremoney.service.excep.common.ParamsException;
import shuhuai.wheremoney.service.excep.common.ServerException;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 预算服务实现类
 * 实现BudgetService接口，提供预算相关的业务逻辑操作，包括预算的创建、更新、删除，以及预算重建等
 */
@Service
public class BudgetServiceImpl implements BudgetService {
    @Resource
    private shuhuai.wheremoney.mapper.LedgerLockMapper ledgerLocks;
    /**
     * 预算Mapper
     */
    @jakarta.annotation.Resource
    private BudgetMapper budgetMapper;
    /**
     * 账本Mapper
     */
    @Resource
    private BookMapper bookMapper;
    /**
     * 支出账单Mapper
     */
    @Resource
    private PayBillMapper payBillMapper;
    /**
     * 退款账单Mapper
     */
    @Resource
    private RefundBillMapper refundBillMapper;

    /**
     * 添加预算
     * @param bookId 账本ID
     * @param billCategoryId 账单分类ID
     * @param limit 预算限额
     * @throws ParamsException 参数错误异常
     * @throws ServerException 服务器错误异常
     */
    @Override
    public void addBudget(Integer bookId, Integer billCategoryId, BigDecimal limit) {
        // 参数校验
        if (bookId == null || billCategoryId == null || limit == null) {
            throw new ParamsException("参数错误");
        }
        // 创建预算实体
        Budget budget = new Budget(bookId, billCategoryId, limit);
        // 插入预算
        Integer result = budgetMapper.insertBudget(budget);
        if (result == null || result != 1) {
            throw new ServerException("服务器错误");
        }
    }

    /**
     * 更新预算
     * @param budget 预算实体
     * @throws ParamsException 参数错误异常
     * @throws ServerException 服务器错误异常
     */
    @Override
    public void updateBudget(Budget budget) {
        // 参数校验
        if (budget == null || budget.getId() == null) {
            throw new ParamsException("参数错误");
        }
        // 更新预算
        Integer result = budgetMapper.updateBudgetById(budget);
        if (result == null || result != 1) {
            throw new ServerException("服务器错误");
        }
    }

    /**
     * 添加预算（带验证）
     * @param bookId 账本ID
     * @param billCategoryId 账单分类ID
     * @param limit 预算限额
     * @throws ParamsException 参数错误异常
     * @throws ServerException 服务器错误异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addBudgetValidated(Integer bookId, Integer billCategoryId, BigDecimal limit) {
        // 参数校验
        if (bookId == null || billCategoryId == null || limit == null || limit.compareTo(BigDecimal.ZERO) < 0) {
            throw new ParamsException("参数错误");
        }
        // 获取账本总预算
        BigDecimal totalLimit = getTotalBudgetByBook(bookId);
        totalLimit = totalLimit == null ? BigDecimal.ZERO : totalLimit;
        // 获取已分配预算
        BigDecimal allocatedLimit = getAllocatedLimitByBook(bookId);
        allocatedLimit = allocatedLimit == null ? BigDecimal.ZERO : allocatedLimit;
        // 检查预算是否超出账本预算额度
        if (allocatedLimit.add(limit).compareTo(totalLimit) > 0) {
            throw new ParamsException("预算超出账本预算额度");
        }
        // 检查该分类是否已经存在预算
        Budget exist = selectBudgetByCategoryId(billCategoryId);
        if (exist != null) {
            throw new ParamsException("该类别已经存在预算");
        }
        // 添加预算
        addBudget(bookId, billCategoryId, limit);
        // 重建账本预算
        rebuildBudgetByBook(bookId);
    }

    /**
     * 更新预算（带验证）
     * @param budgetId 预算ID
     * @param billCategoryId 账单分类ID
     * @param limit 预算限额
     * @param amount 预算金额
     * @param times 预算使用次数
     * @throws ParamsException 参数错误异常
     * @throws ServerException 服务器错误异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBudgetValidated(Integer budgetId, Integer billCategoryId, BigDecimal limit, BigDecimal amount, Integer times) {
        // 检查是否手动修改了已使用金额和次数
        if (amount != null || times != null) {
            throw new ParamsException("预算已使用金额和次数由系统维护，不可手动修改");
        }
        // 获取原预算
        Budget oldBudget = getBudget(budgetId);
        if (oldBudget == null) {
            throw new ParamsException("参数错误");
        }
        // 确定目标分类ID
        Integer targetCategoryId = billCategoryId == null ? oldBudget.getBillCategoryId() : billCategoryId;
        // 检查目标分类是否已经存在预算
        if (!Objects.equals(targetCategoryId, oldBudget.getBillCategoryId())) {
            Budget exist = selectBudgetByCategoryId(targetCategoryId);
            if (exist != null && !Objects.equals(exist.getId(), oldBudget.getId())) {
                throw new ParamsException("该类别已经存在预算");
            }
        }
        // 检查预算限额是否合法
        if (limit != null && limit.compareTo(BigDecimal.ZERO) < 0) {
            throw new ParamsException("参数错误");
        }
        // 确定目标预算限额
        BigDecimal targetLimit = limit == null ? oldBudget.getLimit() : limit;
        targetLimit = targetLimit == null ? BigDecimal.ZERO : targetLimit;
        // 获取账本总预算
        BigDecimal totalLimit = getTotalBudgetByBook(oldBudget.getBookId());
        totalLimit = totalLimit == null ? BigDecimal.ZERO : totalLimit;
        // 获取已分配预算
        BigDecimal allocatedLimit = getAllocatedLimitByBook(oldBudget.getBookId());
        allocatedLimit = allocatedLimit == null ? BigDecimal.ZERO : allocatedLimit;
        // 计算除去当前预算后的已分配预算
        BigDecimal allocatedWithoutCurrent = allocatedLimit.subtract(oldBudget.getLimit() == null ? BigDecimal.ZERO : oldBudget.getLimit());
        // 检查预算是否超出账本预算额度
        if (allocatedWithoutCurrent.add(targetLimit).compareTo(totalLimit) > 0) {
            throw new ParamsException("预算超出账本预算额度");
        }
        // 更新预算分类ID
        if (billCategoryId != null) {
            oldBudget.setBillCategoryId(billCategoryId);
        }
        // 更新预算限额
        if (limit != null) {
            oldBudget.setLimit(limit);
        }
        // 更新预算
        updateBudget(oldBudget);
        // 重建账本预算
        rebuildBudgetByBook(oldBudget.getBookId());
    }

    /**
     * 获取指定预算
     * @param id 预算ID
     * @return 预算实体
     * @throws ParamsException 参数错误异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Budget getBudget(Integer id) {
        // 参数校验
        if (id == null) {
            throw new ParamsException("参数错误");
        }
        // 查询预算
        Budget budget = budgetMapper.selectBudgetById(id);
        if (budget == null) {
            return null;
        }
        // 重建账本预算
        rebuildBudgetByBook(budget.getBookId());
        // 重新查询预算
        return budgetMapper.selectBudgetById(id);
    }

    /**
     * 获取账本的所有预算
     * @param bookId 账本ID
     * @return 预算列表
     * @throws ParamsException 参数错误异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Budget> getBudgetsByBook(Integer bookId) {
        // 参数校验
        if (bookId == null) {
            throw new ParamsException("参数错误");
        }
        // 重建账本预算
        rebuildBudgetByBook(bookId);
        // 查询账本的所有预算
        return budgetMapper.selectBudgetsByBook(bookId);
    }

    /**
     * 获取账本总预算
     * @param bookId 账本ID
     * @return 总预算金额
     */
    @Override
    public BigDecimal getTotalBudgetByBook(Integer bookId) {
        return bookMapper.selectTotalBudgetByBook(bookId);
    }

    /**
     * 获取账本已分配预算
     * @param bookId 账本ID
     * @return 已分配预算金额
     */
    @Override
    public BigDecimal getAllocatedLimitByBook(Integer bookId) {
        return budgetMapper.selectLimitSumByBook(bookId);
    }

    /**
     * 更新账本总预算
     * @param bookId 账本ID
     * @param totalBudget 总预算金额
     * @param usedBudget 已使用预算金额
     */
    @Override
    public void updateTotalBudgetByBook(Integer bookId, BigDecimal totalBudget, BigDecimal usedBudget) {
        bookMapper.updateTotalBudgetByBook(bookId, totalBudget, usedBudget);
    }

    /**
     * 更新账本总预算（带验证）
     * @param bookId 账本ID
     * @param totalBudget 总预算金额
     * @param usedBudget 已使用预算金额
     * @return 是否更新成功
     * @throws ParamsException 参数错误异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTotalBudgetValidated(Integer bookId, BigDecimal totalBudget, BigDecimal usedBudget) {
        // 参数校验
        if (bookId == null) {
            throw new ParamsException("参数错误");
        }
        // 检查是否有更新内容
        if (totalBudget == null && usedBudget == null) {
            return false;
        }
        // 检查是否手动修改了已使用预算
        if (usedBudget != null) {
            throw new ParamsException("已使用预算由系统维护，不可手动修改");
        }
        // 检查总预算是否合法
        if (totalBudget.compareTo(BigDecimal.ZERO) < 0) {
            throw new ParamsException("参数错误");
        }
        // 获取已分配预算
        BigDecimal allocatedLimit = getAllocatedLimitByBook(bookId);
        allocatedLimit = allocatedLimit == null ? BigDecimal.ZERO : allocatedLimit;
        // 检查总预算是否小于已分配预算
        if (totalBudget.compareTo(allocatedLimit) < 0) {
            throw new ParamsException("总预算超出分类预算额度");
        }
        // 更新账本总预算
        updateTotalBudgetByBook(bookId, totalBudget, null);
        // 重建账本预算
        rebuildBudgetByBook(bookId);
        return true;
    }

    /**
     * 根据分类ID获取预算
     * @param billCategoryId 分类ID
     * @return 预算实体
     */
    @Override
    public Budget selectBudgetByCategoryId(Integer billCategoryId) {
        return budgetMapper.selectBudgetByCategoryId(billCategoryId);
    }

    /**
     * 统计退款金额
     * @param refundBills 退款账单列表
     * @return 退款金额映射，键为支出账单ID，值为退款金额
     */
    private Map<Integer, BigDecimal> statisticRefund(List<RefundBill> refundBills) {
        Map<Integer, BigDecimal> result = new HashMap<>();
        if (refundBills == null) {
            return result;
        }
        for (RefundBill refundBill : refundBills) {
            Integer payBillId = refundBill.getPayBillId();
            BigDecimal current = result.getOrDefault(payBillId, BigDecimal.ZERO);
            result.put(payBillId, current.add(refundBill.getAmount()));
        }
        return result;
    }

    /**
     * 获取当前预算周期范围
     * @param beginDate 开始日期
     * @return 预算周期范围，第一个元素为开始时间，第二个元素为结束时间
     */
    private Timestamp[] getCurrentBudgetRange(Integer beginDate) {
        LocalDate now = LocalDate.now(java.time.ZoneId.of("Asia/Shanghai"));
        YearMonth yearMonth = YearMonth.from(now);
        LocalDate startDate;
        if (now.getDayOfMonth() >= beginDate) {
            startDate = yearMonth.atDay(beginDate);
        } else {
            startDate = yearMonth.minusMonths(1).atDay(beginDate);
        }
        LocalDate endDate = startDate.plusMonths(1);
        return new Timestamp[]{Timestamp.valueOf(startDate.atStartOfDay()), Timestamp.valueOf(endDate.atStartOfDay())};
    }

    /**
     * 重建账本预算
     * @param bookId 账本ID
     * @throws ParamsException 参数错误异常
     * @throws ServerException 服务器错误异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rebuildBudgetByBook(Integer bookId) {
        // 参数校验
        if (bookId == null) {
            throw new ParamsException("参数错误");
        }
        if (ledgerLocks.book(bookId) == null) throw new ParamsException("账本不存在");
        // 查询账本
        Book book = bookMapper.selectBookById(bookId);
        if (book == null || book.getBeginDate() == null || book.getBeginDate() < 1 || book.getBeginDate() > 28) {
            throw new ParamsException("参数错误");
        }
        // 获取当前预算周期范围
        Timestamp[] budgetRange = getCurrentBudgetRange(book.getBeginDate());
        Timestamp startTime = budgetRange[0];
        Timestamp endTime = budgetRange[1];
        // 查询账本的所有预算
        List<Budget> budgets = budgetMapper.selectBudgetsByBook(bookId);
        // 预算映射，键为分类ID，值为预算实体
        Map<Integer, Budget> budgetMap = new HashMap<>();
        if (budgets != null) {
            for (Budget budget : budgets) {
                budget.setUsed(BigDecimal.ZERO);
                budget.setTimes(0);
                budgetMap.put(budget.getBillCategoryId(), budget);
            }
        }
        // 查询账本在预算周期内的支出账单
        List<PayBill> payBills = payBillMapper.selectPayBillByBookIdTime(bookId, startTime, endTime);
        // 统计退款金额
        Map<Integer, BigDecimal> refundMap = statisticRefund(refundBillMapper.selectRefundBillByBookIdTime(bookId, startTime, endTime));
        // 总使用预算
        BigDecimal totalUsed = BigDecimal.ZERO;
        if (payBills != null) {
            for (PayBill payBill : payBills) {
                // 计算净支出（支出金额减去退款金额）
                BigDecimal netPay = payBill.getAmount().subtract(refundMap.getOrDefault(payBill.getId(), BigDecimal.ZERO));
                if (netPay.compareTo(BigDecimal.ZERO) < 0) {
                    netPay = BigDecimal.ZERO;
                }
                // 累计总使用预算
                totalUsed = totalUsed.add(netPay);
                // 更新分类预算使用情况
                Budget budget = budgetMap.get(payBill.getBillCategoryId());
                if (budget != null) {
                    budget.setUsed(budget.getUsed().add(netPay));
                    if (netPay.compareTo(BigDecimal.ZERO) > 0) {
                        budget.setTimes(budget.getTimes() + 1);
                    }
                }
            }
        }
        // 更新预算使用情况
        if (budgets != null) {
            for (Budget budget : budgets) {
                Budget updated = new Budget(null, null, null);
                updated.setId(budget.getId());
                updated.setUsed(budget.getUsed());
                updated.setTimes(budget.getTimes());
                Integer result = budgetMapper.updateBudgetById(updated);
                if (result == null || result != 1) {
                    throw new ServerException("服务器错误");
                }
            }
        }
        // 更新账本已使用预算
        bookMapper.updateTotalBudgetByBook(bookId, null, totalUsed);
    }

    /**
     * 更新账本总使用预算相对值
     * @param id 账本ID
     * @param relativeValue 相对值
     * @throws ParamsException 参数错误异常
     * @throws ServerException 服务器错误异常
     */
    @Override
    public void changeTotalUsedBudgetRelative(Integer id, BigDecimal relativeValue) {
        // 参数校验
        if (id == null || relativeValue == null) {
            throw new ParamsException("参数错误");
        }
        // 更新账本总使用预算
        Integer result = bookMapper.updateUsedBudgetRelativeById(id, relativeValue);
        if (result == null || result != 1) {
            throw new ServerException("服务器错误");
        }
    }

    /**
     * 更新分类使用预算相对值
     * @param billCategoryId 分类ID
     * @param relativeValue 相对值
     * @throws ParamsException 参数错误异常
     * @throws ServerException 服务器错误异常
     */
    @Override
    public void changeCategoryUsedBudgetRelative(Integer billCategoryId, BigDecimal relativeValue) {
        // 参数校验
        if (billCategoryId == null || relativeValue == null) {
            throw new ParamsException("参数错误");
        }
        // 更新分类使用预算
        Integer result = budgetMapper.updateUsedRelativeByCategoryId(billCategoryId, relativeValue);
        if (result == null || result != 1) {
            throw new ServerException("服务器错误");
        }
    }

    /**
     * 更新分类使用次数相对值
     * @param billCategoryId 分类ID
     * @param relativeValue 相对值
     * @throws ParamsException 参数错误异常
     * @throws ServerException 服务器错误异常
     */
    @Override
    public void changeCategoryTimesRelative(Integer billCategoryId, Integer relativeValue) {
        // 参数校验
        if (billCategoryId == null || relativeValue == null) {
            throw new ParamsException("参数错误");
        }
        // 更新分类使用次数
        Integer result = budgetMapper.updateTimesRelativeByCategoryId(billCategoryId, relativeValue);
        if (result == null || result != 1) {
            throw new ServerException("服务器错误");
        }
    }

    /**
     * 删除预算
     * @param id 预算ID
     * @throws ParamsException 参数错误异常
     * @throws ServerException 服务器错误异常
     */
    @Override
    public void deleteBudget(Integer id) {
        // 参数校验
        if (id == null) {
            throw new ParamsException("参数错误");
        }
        // 删除预算
        Integer result = budgetMapper.deleteBudgetById(id);
        if (result == null || result != 1) {
            throw new ServerException("服务器错误");
        }
    }
}
