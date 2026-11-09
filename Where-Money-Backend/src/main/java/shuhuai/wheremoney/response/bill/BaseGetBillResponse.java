package shuhuai.wheremoney.response.bill;

import shuhuai.wheremoney.entity.BaseBill;
import shuhuai.wheremoney.type.BillType;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 基础账单响应类
 * 用于返回账单的基本信息
 */
public class BaseGetBillResponse {
    private Integer id;
    private BigDecimal amount;
    private BillType type;
    private Timestamp billTime;
    private String remark;

    /**
     * 构造方法
     *
     * @param id       账单ID
     * @param amount   金额
     * @param type     账单类型
     * @param billTime 账单时间
     * @param remark   备注
     */
    public BaseGetBillResponse(Integer id, BigDecimal amount, BillType type, Timestamp billTime, String remark) {
        this.id = id;
        this.amount = amount;
        this.type = type;
        this.billTime = billTime;
        this.remark = remark;
    }

    /**
     * 构造方法
     *
     * @param bill 基础账单对象
     * @param type 账单类型
     */
    public BaseGetBillResponse(BaseBill bill, BillType type) {
        this.id = bill.getId();
        this.amount = bill.getAmount();
        this.type = type;
        this.billTime = bill.getBillTime();
        this.remark = bill.getRemark();
    }

    /**
     * 获取账单ID
     *
     * @return 账单ID
     */
    public Integer getId() {
        return id;
    }

    /**
     * 设置账单ID
     *
     * @param id 账单ID
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 获取金额
     *
     * @return 金额
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * 设置金额
     *
     * @param amount 金额
     */
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    /**
     * 获取账单类型
     *
     * @return 账单类型
     */
    public BillType getType() {
        return type;
    }

    /**
     * 设置账单类型
     *
     * @param type 账单类型
     */
    public void setType(BillType type) {
        this.type = type;
    }

    /**
     * 获取备注
     *
     * @return 备注
     */
    public String getRemark() {
        return remark;
    }

    /**
     * 设置备注
     *
     * @param remark 备注
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }

    /**
     * 获取账单时间
     *
     * @return 账单时间
     */
    public Timestamp getBillTime() {
        return billTime;
    }

    /**
     * 设置账单时间
     *
     * @param billTime 账单时间
     */
    public void setBillTime(Timestamp billTime) {
        this.billTime = billTime;
    }
}