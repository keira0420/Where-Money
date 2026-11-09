package shuhuai.wheremoney.response.bill;

import shuhuai.wheremoney.entity.BaseBill;
import shuhuai.wheremoney.type.BillType;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 获取支出账单响应类
 * 用于返回支出账单的详细信息
 */
public class GetPayBillResponse extends BaseGetBillResponse {
    private String payAsset;
    private String billCategory;

    private Boolean refunded;

    /**
     * 构造方法
     *
     * @param id           账单ID
     * @param amount       金额
     * @param remark       备注
     * @param billTime     账单时间
     * @param payAsset     支出资产名称
     * @param billCategory 账单分类名称
     * @param refunded     是否已退款
     */
    public GetPayBillResponse(Integer id, BigDecimal amount, String remark, Timestamp billTime, String payAsset, String billCategory, Boolean refunded) {
        super(id, amount, BillType.支出, billTime, remark);
        this.payAsset = payAsset;
        this.billCategory = billCategory;
        this.refunded = refunded;
    }

    /**
     * 构造方法
     *
     * @param bill         基础账单对象
     * @param payAsset     支出资产名称
     * @param billCategory 账单分类名称
     * @param refunded     是否已退款
     */
    public GetPayBillResponse(BaseBill bill, String payAsset, String billCategory, Boolean refunded) {
        super(bill, BillType.支出);
        this.payAsset = payAsset;
        this.billCategory = billCategory;
        this.refunded = refunded;
    }

    /**
     * 获取支出资产名称
     *
     * @return 支出资产名称
     */
    public String getPayAsset() {
        return payAsset;
    }

    /**
     * 设置支出资产名称
     *
     * @param payAsset 支出资产名称
     */
    public void setPayAsset(String payAsset) {
        this.payAsset = payAsset;
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

    /**
     * 获取是否已退款
     *
     * @return 是否已退款
     */
    public Boolean getRefunded() {
        return refunded;
    }

    /**
     * 设置是否已退款
     *
     * @param refunded 是否已退款
     */
    public void setRefunded(Boolean refunded) {
        this.refunded = refunded;
    }
}