package shuhuai.wheremoney.response.bill;

import shuhuai.wheremoney.entity.BaseBill;
import shuhuai.wheremoney.type.BillType;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 获取收入账单响应类
 * 用于返回收入账单的详细信息
 */
public class GetIncomeBillResponse extends BaseGetBillResponse {
    private String incomeAsset;
    private String billCategory;

    /**
     * 构造方法
     *
     * @param id           账单ID
     * @param amount       金额
     * @param remark       备注
     * @param billTime     账单时间
     * @param incomeAsset  收入资产名称
     * @param billCategory 账单分类名称
     */
    public GetIncomeBillResponse(Integer id, BigDecimal amount, String remark, Timestamp billTime, String incomeAsset, String billCategory) {
        super(id, amount, BillType.收入, billTime, remark);
        this.incomeAsset = incomeAsset;
        this.billCategory = billCategory;
    }

    /**
     * 构造方法
     *
     * @param bill         基础账单对象
     * @param incomeAsset  收入资产名称
     * @param billCategory 账单分类名称
     */
    public GetIncomeBillResponse(BaseBill bill, String incomeAsset, String billCategory) {
        super(bill, BillType.收入);
        this.incomeAsset = incomeAsset;
        this.billCategory = billCategory;
    }

    /**
     * 获取收入资产名称
     *
     * @return 收入资产名称
     */
    public String getIncomeAsset() {
        return incomeAsset;
    }

    /**
     * 设置收入资产名称
     *
     * @param incomeAsset 收入资产名称
     */
    public void setIncomeAsset(String incomeAsset) {
        this.incomeAsset = incomeAsset;
    }

    /**
     * 获取账单分类名称
     *
     * @return 账单分类名称
     */
    public String getBillCategory() {
        return billCategory;
    }

    /**
     * 设置账单分类名称
     *
     * @param billCategory 账单分类名称
     */
    public void setBillCategory(String billCategory) {
        this.billCategory = billCategory;
    }
}