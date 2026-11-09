package shuhuai.wheremoney.response.bill;

import shuhuai.wheremoney.entity.BaseBill;
import shuhuai.wheremoney.entity.RefundBill;
import shuhuai.wheremoney.type.BillType;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 获取退款账单响应类
 * 用于返回退款账单的详细信息
 */
public class GetRefundBillResponse extends BaseGetBillResponse {
    private Integer payBillId;
    private String refundAsset;

    /**
     * 构造方法
     *
     * @param id           账单ID
     * @param amount       金额
     * @param remark       备注
     * @param billTime     账单时间
     * @param payBillId    关联的支出账单ID
     * @param refundAsset  退款资产名称
     */
    public GetRefundBillResponse(Integer id, BigDecimal amount, String remark, Timestamp billTime, Integer payBillId, String refundAsset) {
        super(id, amount, BillType.退款, billTime, remark);
        this.payBillId = payBillId;
        this.refundAsset = refundAsset;
    }

    /**
     * 构造方法
     *
     * @param bill         基础账单对象
     * @param refundAsset  退款资产名称
     */
    public GetRefundBillResponse(BaseBill bill, String refundAsset) {
        super(bill, BillType.退款);
        this.payBillId = ((RefundBill) bill).getPayBillId();
        this.refundAsset = refundAsset;
    }

    /**
     * 获取关联的支出账单ID
     *
     * @return 关联的支出账单ID
     */
    public Integer getPayBillId() {
        return payBillId;
    }

    /**
     * 设置关联的支出账单ID
     *
     * @param payBillId 关联的支出账单ID
     */
    public void setPayBillId(Integer payBillId) {
        this.payBillId = payBillId;
    }

    /**
     * 获取退款资产名称
     *
     * @return 退款资产名称
     */
    public String getRefundAsset() {
        return refundAsset;
    }

    /**
     * 设置退款资产名称
     *
     * @param refundAsset 退款资产名称
     */
    public void setRefundAsset(String refundAsset) {
        this.refundAsset = refundAsset;
    }
}