package shuhuai.wheremoney.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 退款账单实体类
 * 用于表示用户的退款记录
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
public class RefundBill extends BaseBill {
    private Integer payBillId;
    private Integer refundAssetId;

    /**
     * 构造方法
     *
     * @param id             账单ID
     * @param bookId         账本ID
     * @param payBillId      关联的支出账单ID
     * @param refundAssetId  退款资产ID
     * @param amount         金额
     * @param billTime       账单时间
     * @param remark         备注
     * @param image          图片
     */
    public RefundBill(Integer id, Integer bookId, Integer payBillId, Integer refundAssetId, BigDecimal amount, Timestamp billTime, String remark, byte[] image, String imageContentType) {
        super(id, bookId, amount, billTime, remark, image, imageContentType);
        this.payBillId = payBillId;
        this.refundAssetId = refundAssetId;
    }

    /**
     * 构造方法
     *
     * @param bookId         账本ID
     * @param payBillId      关联的支出账单ID
     * @param refundAssetId  退款资产ID
     * @param amount         金额
     * @param billTime       账单时间
     * @param remark         备注
     * @param image          图片
     */
    public RefundBill(Integer bookId, Integer payBillId, Integer refundAssetId, BigDecimal amount, Timestamp billTime, String remark, byte[] image, String imageContentType) {
        super(bookId, amount, billTime, remark, image, imageContentType);
        this.payBillId = payBillId;
        this.refundAssetId = refundAssetId;
    }

}