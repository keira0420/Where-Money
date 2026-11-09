package shuhuai.wheremoney.response.bill;

import shuhuai.wheremoney.entity.BaseBill;
import shuhuai.wheremoney.entity.TransferBill;
import shuhuai.wheremoney.type.BillType;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 获取转账账单响应类
 * 用于返回转账账单的详细信息
 */
public class GetTransferBillResponse extends BaseGetBillResponse {
    private String inAsset;
    private String outAsset;
    private BigDecimal transferFee;

    /**
     * 构造方法
     *
     * @param id           账单ID
     * @param amount       金额
     * @param remark       备注
     * @param billTime     账单时间
     * @param inAsset      转入资产名称
     * @param outAsset     转出资产名称
     * @param transferFee  转账手续费
     */
    public GetTransferBillResponse(Integer id, BigDecimal amount, String remark, Timestamp billTime, String inAsset, String outAsset, BigDecimal transferFee) {
        super(id, amount, BillType.转账, billTime, remark);
        this.inAsset = inAsset;
        this.outAsset = outAsset;
        this.transferFee = transferFee;
    }

    /**
     * 构造方法
     *
     * @param bill     基础账单对象
     * @param inAsset  转入资产名称
     * @param outAsset 转出资产名称
     */
    public GetTransferBillResponse(BaseBill bill, String inAsset, String outAsset) {
        super(bill, BillType.转账);
        this.inAsset = inAsset;
        this.outAsset = outAsset;
        this.transferFee = ((TransferBill) bill).getTransferFee();
    }

    /**
     * 获取转入资产名称
     *
     * @return 转入资产名称
     */
    public String getInAsset() {
        return inAsset;
    }

    /**
     * 设置转入资产名称
     *
     * @param inAsset 转入资产名称
     */
    public void setInAsset(String inAsset) {
        this.inAsset = inAsset;
    }

    /**
     * 获取转出资产名称
     *
     * @return 转出资产名称
     */
    public String getOutAsset() {
        return outAsset;
    }

    /**
     * 设置转出资产名称
     *
     * @param outAsset 转出资产名称
     */
    public void setOutAsset(String outAsset) {
        this.outAsset = outAsset;
    }

    /**
     * 获取转账手续费
     *
     * @return 转账手续费
     */
    public BigDecimal getTransferFee() {
        return transferFee;
    }

    /**
     * 设置转账手续费
     *
     * @param transferFee 转账手续费
     */
    public void setTransferFee(BigDecimal transferFee) {
        this.transferFee = transferFee;
    }
}