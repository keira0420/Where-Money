package shuhuai.wheremoney.service;

import shuhuai.wheremoney.entity.Asset;
import shuhuai.wheremoney.type.AssetType;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * 资产服务接口
 * 提供资产相关的业务逻辑操作，包括资产的创建、查询、更新，以及资产统计功能
 */
public interface AssetService {
    /**
     * 添加资产
     * @param userId 用户ID
     * @param assetName 资产名称
     * @param balance 资产余额
     * @param type 资产类型
     * @param billDate 账单日
     * @param repayDate 还款日
     * @param quota 额度
     * @param inTotal 是否计入总资产
     * @param svg 资产图标
     */
    void addAsset(Integer userId, String assetName, BigDecimal balance, AssetType type,
                  Integer billDate, Integer repayDate, BigDecimal quota, Boolean inTotal, String svg);

    /**
     * 获取用户的所有资产
     * @param userId 用户ID
     * @return 资产列表
     */
    List<Asset> getAllAsset(Integer userId);

    /**
     * 获取指定资产
     * @param id 资产ID
     * @return 资产实体
     */
    Asset getAsset(Integer id);

    /**
     * 更新资产
     * @param asset 资产实体
     */
    void updateAsset(Asset asset);

    /**
     * 获取资产日统计
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 日统计数据
     */
    List<Map<String, Object>> getDayStatistic(Integer userId, Timestamp startTime, Timestamp endTime);

    /**
     * 更新资产余额相对值
     * @param id 资产ID
     * @param relativeValue 相对值
     */
    void changeBalanceRelative(Integer id, BigDecimal relativeValue);
}