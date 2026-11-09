package shuhuai.wheremoney.entity;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import shuhuai.wheremoney.type.AssetType;

import java.math.BigDecimal;

/**
 * 资产实体类
 * 用于表示用户的资产信息
 */
@AllArgsConstructor
@NoArgsConstructor
public class Asset {
    private Integer id;
    private Integer userId;
    private AssetType type;
    private BigDecimal balance;
    private String assetName;
    private Integer billDate;
    private Integer repayDate;
    private BigDecimal quota;
    private Boolean inTotal;
    private String svg;

    /**
     * 构造方法
     *
     * @param userId     用户ID
     * @param type       资产类型
     * @param balance    余额
     * @param assetName  资产名称
     * @param billDate   账单日
     * @param repayDate  还款日
     * @param quota      额度
     * @param inTotal    是否计入总览
     * @param svg        SVG图标
     */
    public Asset(Integer userId, AssetType type, BigDecimal balance, String assetName, Integer billDate, Integer repayDate, BigDecimal quota, Boolean inTotal, String svg) {
        this.userId = userId;
        this.type = type;
        this.balance = balance;
        this.assetName = assetName;
        this.billDate = billDate;
        this.repayDate = repayDate;
        this.quota = quota;
        this.inTotal = inTotal;
        this.svg = svg;
    }

    /**
     * 获取资产ID
     *
     * @return 资产ID
     */
    public Integer getId() {
        return id;
    }

    /**
     * 设置资产ID
     *
     * @param id 资产ID
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 获取用户ID
     *
     * @return 用户ID
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * 设置用户ID
     *
     * @param userId 用户ID
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    /**
     * 获取资产类型
     *
     * @return 资产类型
     */
    public AssetType getType() {
        return type;
    }

    /**
     * 设置资产类型
     *
     * @param type 资产类型
     */
    public void setType(AssetType type) {
        this.type = type;
    }

    /**
     * 获取余额
     *
     * @return 余额
     */
    public BigDecimal getBalance() {
        return balance;
    }

    /**
     * 设置余额
     *
     * @param balance 余额
     */
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    /**
     * 获取资产名称
     *
     * @return 资产名称
     */
    public String getAssetName() {
        return assetName;
    }

    /**
     * 设置资产名称
     *
     * @param assetName 资产名称
     */
    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    /**
     * 获取账单日
     *
     * @return 账单日
     */
    public Integer getBillDate() {
        return billDate;
    }

    /**
     * 设置账单日
     *
     * @param billDate 账单日
     */
    public void setBillDate(Integer billDate) {
        this.billDate = billDate;
    }

    /**
     * 获取还款日
     *
     * @return 还款日
     */
    public Integer getRepayDate() {
        return repayDate;
    }

    /**
     * 设置还款日
     *
     * @param repayDate 还款日
     */
    public void setRepayDate(Integer repayDate) {
        this.repayDate = repayDate;
    }

    /**
     * 获取额度
     *
     * @return 额度
     */
    public BigDecimal getQuota() {
        return quota;
    }

    /**
     * 设置额度
     *
     * @param quota 额度
     */
    public void setQuota(BigDecimal quota) {
        this.quota = quota;
    }

    /**
     * 获取是否计入总览
     *
     * @return 是否计入总览
     */
    public Boolean getInTotal() {
        return inTotal;
    }

    /**
     * 设置是否计入总览
     *
     * @param inTotal 是否计入总览
     */
    public void setInTotal(Boolean inTotal) {
        this.inTotal = inTotal;
    }

    /**
     * 获取SVG图标
     *
     * @return SVG图标
     */
    public String getSvg() {
        return svg;
    }

    /**
     * 设置SVG图标
     *
     * @param svg SVG图标
     */
    public void setSvg(String svg) {
        this.svg = svg;
    }
}