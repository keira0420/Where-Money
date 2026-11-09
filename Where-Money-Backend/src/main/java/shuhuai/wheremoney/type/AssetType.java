package shuhuai.wheremoney.type;

/**
 * 资产类型枚举
 * 定义了系统支持的资产类型
 */
public enum AssetType {
    /**
     * 资金资产
     */
    资金("资金"),
    /**
     * 信用卡资产
     */
    信用卡("信用卡"),
    /**
     * 充值资产
     */
    充值("充值"),
    /**
     * 投资理财资产
     */
    投资理财("投资理财");

    /**
     * 资产类型名称
     */
    private String type;

    /**
     * 构造方法
     *
     * @param type 资产类型名称
     */
    AssetType(String type) {
        this.type = type;
    }

    /**
     * 根据类型名称获取资产类型枚举
     *
     * @param type 资产类型名称
     * @return 对应的资产类型枚举
     */
    public static AssetType getAssetTypeEnum(String type) {
        return switch (type) {
            case "资金" -> 资金;
            case "信用卡" -> 信用卡;
            case "充值" -> 充值;
            case "投资理财" -> 投资理财;
            default -> null;
        };
    }

    /**
     * 获取资产类型名称
     *
     * @return 资产类型名称
     */
    public String getType() {
        return type;
    }

    /**
     * 设置资产类型名称
     *
     * @param type 资产类型名称
     */
    public void setType(String type) {
        this.type = type;
    }
}