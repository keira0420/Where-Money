package shuhuai.wheremoney.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 支付账单实体类
 * 用于表示账本中的支付账单信息
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
public class PayBill extends BaseBill implements Serializable {
    private Integer payAssetId;
    private Integer billCategoryId;
    private Boolean refunded;

    /**
     * 构造方法
     *
     * @param id       账单ID
     * @param refunded 是否已退款
     */
    public PayBill(Integer id, Boolean refunded) {
        super(id);
        this.refunded = refunded;
    }

    /**
     * 构造方法
     *
     * @param id             账单ID
     * @param bookId         账本ID
     * @param payAssetId     支付资产ID
     * @param billCategoryId 账单分类ID
     * @param amount         金额
     * @param billTime       账单时间
     * @param remark         备注
     * @param refunded       是否已退款
     * @param image          图片
     */
    public PayBill(Integer id, Integer bookId, Integer payAssetId, Integer billCategoryId, BigDecimal amount, Timestamp billTime, String remark, Boolean refunded, byte[] image, String imageContentType) {
        super(id, bookId, amount, billTime, remark, image, imageContentType);
        this.payAssetId = payAssetId;
        this.billCategoryId = billCategoryId;
        this.refunded = refunded;
    }

    /**
     * 构造方法
     *
     * @param bookId         账本ID
     * @param payAssetId     支付资产ID
     * @param billCategoryId 账单分类ID
     * @param amount         金额
     * @param billTime       账单时间
     * @param remark         备注
     * @param refunded       是否已退款
     * @param image          图片
     */
    public PayBill(Integer bookId, Integer payAssetId, Integer billCategoryId, BigDecimal amount, Timestamp billTime, String remark, Boolean refunded, byte[] image, String imageContentType) {
        super(bookId, amount, billTime, remark, image, imageContentType);
        this.payAssetId = payAssetId;
        this.billCategoryId = billCategoryId;
        this.refunded = refunded;
    }

}