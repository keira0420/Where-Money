package shuhuai.wheremoney.entity;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 基础账单实体类
 * 所有类型的账单都继承自此基类
 */
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class BaseBill implements Serializable {
    private Integer id;
    private Integer bookId;
    private BigDecimal amount;
    private Timestamp billTime;
    private String remark;
    private byte[] image;
    private String imageContentType;

    /**
     * 构造方法
     *
     * @param id 账单ID
     */
    public BaseBill(Integer id) {
        this.id = id;
    }

    /**
     * 构造方法
     *
     * @param bookId   账本ID
     * @param amount   金额
     * @param billTime 账单时间
     * @param remark   备注
     * @param image    图片
     */
    public BaseBill(Integer bookId, BigDecimal amount, Timestamp billTime, String remark, byte[] image, String imageContentType) {
        this.bookId = bookId;
        this.amount = amount;
        this.billTime = billTime;
        this.remark = remark;
        this.image = image;
        this.imageContentType = imageContentType;
    }

}