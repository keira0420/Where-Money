package shuhuai.wheremoney.mapper;

import org.apache.ibatis.annotations.Mapper;
import shuhuai.wheremoney.entity.Asset;

import java.math.BigDecimal;
import java.util.List;

/**
 * 资产Mapper接口
 * 用于操作数据库中的asset表，提供资产的CRUD操作和统计功能
 */
@Mapper
public interface AssetMapper {
    /**
     * 选择性插入资产
     * @param asset 资产实体
     * @return 影响的行数
     */
    Integer insertAssetSelective(Asset asset);

    /**
     * 根据ID选择性更新资产
     * @param asset 资产实体
     * @return 影响的行数
     */
    Integer updateAssetSelectiveById(Asset asset);

    /**
     * 根据ID更新资产余额相对值
     * @param id 资产ID
     * @param relativeValue 相对值
     * @return 影响的行数
     */
    Integer updateBalanceRelativeById(Integer id, BigDecimal relativeValue);

    /**
     * 根据用户ID查询资产
     * @param userId 用户ID
     * @return 资产列表
     */
    List<Asset> selectAssetByUserId(Integer userId);

    /**
     * 根据ID查询资产
     * @param id 资产ID
     * @return 资产实体
     */
    Asset selectAssetById(Integer id);

    /**
     * 根据用户ID查询总资产
     * @param userId 用户ID
     * @return 总资产金额
     */
    BigDecimal selectTotalAssetByUserId(Integer userId);
}