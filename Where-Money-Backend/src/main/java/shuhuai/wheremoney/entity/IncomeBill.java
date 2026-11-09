package shuhuai.wheremoney.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 收入账单实体类
 * 用于表示用户的收入记录
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
public class IncomeBill extends BaseBill {
    private Integer incomeAssetId;
    private Integer billCategoryId;

    /**
     * 构造方法
     *
     * @param id             账单ID
     * @param bookId         账本ID
     * @param incomeAssetId  收入资产ID
     * @param billCategoryId 账单分类ID
     * @param amount         金额
     * @param billTime       账单时间
     * @param remark         备注
     * @param image          图片
     */
    public IncomeBill(Integer id, Integer bookId, Integer incomeAssetId, Integer billCategoryId, BigDecimal amount, Timestamp billTime, String remark, byte[] image, String imageContentType) {
        super(id, bookId, amount, billTime, remark, image, imageContentType);
        this.incomeAssetId = incomeAssetId;
        this.billCategoryId = billCategoryId;
    }

    /**
     * 构造方法
     *
     * @param bookId         账本ID
     * @param incomeAssetId  收入资产ID
     * @param billCategoryId 账单分类ID
     * @param amount         金额
     * @param billTime       账单时间
     * @param remark         备注
     * @param image          图片
     */
    public IncomeBill(Integer bookId, Integer incomeAssetId, Integer billCategoryId, BigDecimal amount, Timestamp billTime, String remark, byte[] image, String imageContentType) {
        super(bookId, amount, billTime, remark, image, imageContentType);
        this.incomeAssetId = incomeAssetId;
        this.billCategoryId = billCategoryId;
    }

}