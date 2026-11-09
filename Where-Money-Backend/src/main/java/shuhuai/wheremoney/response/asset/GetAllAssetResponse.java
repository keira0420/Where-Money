package shuhuai.wheremoney.response.asset;

import shuhuai.wheremoney.entity.Asset;

import java.math.BigDecimal;
import java.util.List;

/**
 * 获取所有资产响应类
 * 用于返回用户的所有资产信息及统计数据
 */
public class GetAllAssetResponse {
    private List<Asset> assetList;
    private BigDecimal netAsset;
    private BigDecimal totalAsset;
    private BigDecimal totalLiabilities;

    /**
     * 构造方法
     * 初始化资产列表并计算总资产、总负债和净资产
     *
     * @param assetList 资产列表
     */
    public GetAllAssetResponse(List<Asset> assetList) {
        this.assetList = assetList;
        this.totalAsset = new BigDecimal(0);
        this.totalLiabilities = new BigDecimal(0);
        for (Asset asset : assetList) {
            if (asset.getInTotal()) {
                if (asset.getType().getType().equals("信用卡")) {
                    this.totalLiabilities = this.totalLiabilities.add(asset.getBalance());
                } else {
                    this.totalAsset = this.totalAsset.add(asset.getBalance());
                }
            }
        }
        this.netAsset = this.totalAsset.add(this.totalLiabilities);
    }

    /**
     * 获取资产列表
     *
     * @return 资产列表
     */
    public List<Asset> getAssetList() {
        return assetList;
    }

    /**
     * 设置资产列表
     *
     * @param assetList 资产列表
     */
    public void setAssetList(List<Asset> assetList) {
        this.assetList = assetList;
    }

    /**
     * 获取净资产
     *
     * @return 净资产
     */
    public BigDecimal getNetAsset() {
        return netAsset;
    }

    /**
     * 设置净资产
     *
     * @param netAsset 净资产
     */
    public void setNetAsset(BigDecimal netAsset) {
        this.netAsset = netAsset;
    }

    /**
     * 获取总资产
     *
     * @return 总资产
     */
    public BigDecimal getTotalAsset() {
        return totalAsset;
    }

    /**
     * 设置总资产
     *
     * @param totalAsset 总资产
     */
    public void setTotalAsset(BigDecimal totalAsset) {
        this.totalAsset = totalAsset;
    }

    /**
     * 获取总负债
     *
     * @return 总负债
     */
    public BigDecimal getTotalLiabilities() {
        return totalLiabilities;
    }

    /**
     * 设置总负债
     *
     * @param totalLiabilities 总负债
     */
    public void setTotalLiabilities(BigDecimal totalLiabilities) {
        this.totalLiabilities = totalLiabilities;
    }
}