package shuhuai.wheremoney.entity;

import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 预算实体类
 * 用于表示账本中各分类的预算信息
 */
@AllArgsConstructor
public class Budget implements Serializable {
    private Integer id;
    private Integer bookId;
    private Integer billCategoryId;
    private BigDecimal used;
    private BigDecimal limit;
    private Integer times;

    /**
     * 构造方法
     *
     * @param bookId          账本ID
     * @param billCategoryId  账单分类ID
     * @param limit           预算限额
     */
    public Budget(Integer bookId, Integer billCategoryId, BigDecimal limit) {
        this.bookId = bookId;
        this.billCategoryId = billCategoryId;
        this.limit = limit;
    }

    /**
     * 获取预算限额
     *
     * @return 预算限额
     */
    public BigDecimal getLimit() {
        return limit;
    }

    /**
     * 设置预算限额
     *
     * @param limit 预算限额
     */
    public void setLimit(BigDecimal limit) {
        this.limit = limit;
    }

    /**
     * 获取预算ID
     *
     * @return 预算ID
     */
    public Integer getId() {
        return id;
    }

    /**
     * 设置预算ID
     *
     * @param id 预算ID
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 获取账本ID
     *
     * @return 账本ID
     */
    public Integer getBookId() {
        return bookId;
    }

    /**
     * 设置账本ID
     *
     * @param bookId 账本ID
     */
    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }

    /**
     * 获取账单分类ID
     *
     * @return 账单分类ID
     */
    public Integer getBillCategoryId() {
        return billCategoryId;
    }

    /**
     * 设置账单分类ID
     *
     * @param billCategoryId 账单分类ID
     */
    public void setBillCategoryId(Integer billCategoryId) {
        this.billCategoryId = billCategoryId;
    }

    /**
     * 获取已使用金额
     *
     * @return 已使用金额
     */
    public BigDecimal getUsed() {
        return used;
    }

    /**
     * 设置已使用金额
     *
     * @param used 已使用金额
     */
    public void setUsed(BigDecimal used) {
        this.used = used;
    }

    /**
     * 获取使用次数
     *
     * @return 使用次数
     */
    public Integer getTimes() {
        return times;
    }

    /**
     * 设置使用次数
     *
     * @param times 使用次数
     */
    public void setTimes(Integer times) {
        this.times = times;
    }
}