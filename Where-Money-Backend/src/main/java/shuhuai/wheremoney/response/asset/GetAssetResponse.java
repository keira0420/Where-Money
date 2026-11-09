package shuhuai.wheremoney.response.asset;

import shuhuai.wheremoney.entity.Asset;

/**
 * 获取单个资产响应类
 * 用于返回单个资产的详细信息
 */
public class GetAssetResponse {
    private Asset asset;

    /**
     * 构造方法
     *
     * @param asset 资产对象
     */
    public GetAssetResponse(Asset asset) {
        this.asset = asset;
    }

    /**
     * 获取资产对象
     *
     * @return 资产对象
     */
    public Asset getAsset() {
        return asset;
    }

    /**
     * 设置资产对象
     *
     * @param asset 资产对象
     */
    public void setAsset(Asset asset) {
        this.asset = asset;
    }
}