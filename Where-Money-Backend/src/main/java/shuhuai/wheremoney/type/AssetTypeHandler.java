package shuhuai.wheremoney.type;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 资产类型MyBatis类型处理器
 * 用于处理AssetType枚举类型和数据库VARCHAR类型之间的转换
 */
@MappedJdbcTypes(JdbcType.VARCHAR)
@MappedTypes(AssetType.class)
public class AssetTypeHandler extends BaseTypeHandler<AssetType> {
    /**
     * 设置非空参数
     *
     * @param preparedStatement 预编译语句对象
     * @param index             参数索引
     * @param assetType         资产类型枚举
     * @param jdbcType          JDBC类型
     * @throws SQLException SQL异常
     */
    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int index, AssetType assetType, JdbcType jdbcType) throws SQLException {
        preparedStatement.setString(index, assetType.getType());
    }

    /**
     * 从结果集中获取可为空的结果（通过字段名）
     *
     * @param resultSet 结果集对象
     * @param field     字段名
     * @return 资产类型枚举
     * @throws SQLException SQL异常
     */
    @Override
    public AssetType getNullableResult(ResultSet resultSet, String field) throws SQLException {
        return AssetType.getAssetTypeEnum(resultSet.getString(field));
    }

    /**
     * 从结果集中获取可为空的结果（通过索引）
     *
     * @param resultSet 结果集对象
     * @param index     索引
     * @return 资产类型枚举
     * @throws SQLException SQL异常
     */
    @Override
    public AssetType getNullableResult(ResultSet resultSet, int index) throws SQLException {
        return AssetType.getAssetTypeEnum(resultSet.getString(index));
    }

    /**
     * 从CallableStatement中获取可为空的结果
     *
     * @param callableStatement 调用语句对象
     * @param index             索引
     * @return 资产类型枚举
     * @throws SQLException SQL异常
     */
    @Override
    public AssetType getNullableResult(CallableStatement callableStatement, int index) throws SQLException {
        return AssetType.getAssetTypeEnum(callableStatement.getString(index));
    }
}