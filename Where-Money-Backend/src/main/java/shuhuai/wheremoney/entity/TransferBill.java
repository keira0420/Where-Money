package shuhuai.wheremoney.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 转账账单实体类
 * 用于表示用户的转账记录
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
public class TransferBill extends BaseBill {
    private Integer inAssetId;
    private Integer outAssetId;
    private BigDecimal transferFee;

    /**
     * 构造方法
     *
     * @param id             账单ID
     * @param bookId         账本ID
     * @param inAssetId      转入资产ID
     * @param outAssetId     转出资产ID
     * @param amount         金额
     * @param tranferFee     转账手续费
     * @param billTime       账单时间
     * @param remark         备注
     * @param image          图片
     */
    public TransferBill(Integer id, Integer bookId, Integer inAssetId, Integer outAssetId, BigDecimal amount, BigDecimal tranferFee, Timestamp billTime, String remark,
                        byte[] image, String imageContentType) {
        super(id, bookId, amount, billTime, remark, image, imageContentType);
        this.inAssetId = inAssetId;
        this.outAssetId = outAssetId;
        this.transferFee = tranferFee;
    }

    /**
     * 构造方法
     *
     * @param bookId         账本ID
     * @param inAssetId      转入资产ID
     * @param outAssetId     转出资产ID
     * @param amount         金额
     * @param tranferFee     转账手续费
     * @param billTime       账单时间
     * @param remark         备注
     * @param image          图片
     */
    public TransferBill(Integer bookId, Integer inAssetId, Integer outAssetId, BigDecimal amount, BigDecimal tranferFee, Timestamp billTime, String remark, byte[] image, String imageContentType) {
        super(bookId, amount, billTime, remark, image, imageContentType);
        this.inAssetId = inAssetId;
        this.outAssetId = outAssetId;
        this.transferFee = tranferFee;
    }

}