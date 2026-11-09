package shuhuai.wheremoney.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import shuhuai.wheremoney.entity.Asset;
import shuhuai.wheremoney.response.Response;
import shuhuai.wheremoney.response.asset.DayStatisticTimeResponse;
import shuhuai.wheremoney.response.asset.GetAllAssetResponse;
import shuhuai.wheremoney.response.asset.GetAssetResponse;
import shuhuai.wheremoney.service.AssetService;
import shuhuai.wheremoney.service.excep.common.ParamsException;
import shuhuai.wheremoney.type.AssetType;
import shuhuai.wheremoney.utils.TokenValidator;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * 资产管理控制器
 * 处理资产相关的HTTP请求，包括新建、修改、查询资产等操作
 */
@RestController
@RequestMapping("/api/asset")
@Tag(name = "资产管理")
@Slf4j
public class AssetController extends BaseController {
    /**
     * 资产服务实例，用于处理资产相关的业务逻辑
     */
    @Resource
    private AssetService assetService;

    /**
     * 新建资产
     * @param assetName 资产名称
     * @param balance 资产余额
     * @param type 资产类型
     * @param billDate 账单日（可选）
     * @param repayDate 还款日（可选）
     * @param quota 额度（可选）
     * @param inTotal 是否计入总资产
     * @param svg 资产图标
     * @return 新建资产结果
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "token过期"),
            @ApiResponse(responseCode = "422", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    @RequestMapping(value = "/asset", method = RequestMethod.POST)
    @Operation(summary = "新建资产")
    public Response<Object> addAsset(@RequestParam String assetName, @RequestParam BigDecimal balance, @RequestParam AssetType type,
                                     Integer billDate, Integer repayDate, BigDecimal quota,
                                     @RequestParam Boolean inTotal, @RequestParam String svg) {
        // 从token中获取用户ID
        Integer userId = Integer.parseInt(TokenValidator.getUser().get("userId"));
        // 调用服务层添加资产
        assetService.addAsset(userId, assetName, balance, type, billDate, repayDate, quota, inTotal, svg);
        return new Response<>(200, "新建资产成功", null);
    }

    /**
     * 修改资产
     * @param assetId 资产ID
     * @param balance 资产余额（可选）
     * @param assetName 资产名称（可选）
     * @param billDate 账单日（可选）
     * @param repayDate 还款日（可选）
     * @param quota 额度（可选）
     * @param inTotal 是否计入总资产（可选）
     * @param svg 资产图标（可选）
     * @return 修改资产结果
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "token过期"),
            @ApiResponse(responseCode = "422", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    @RequestMapping(value = "/asset", method = RequestMethod.PATCH)
    @Operation(summary = "修改资产")
    public Response<Object> updateAsset(@RequestParam Integer assetId, BigDecimal balance, String assetName,
                                        Integer billDate, Integer repayDate, BigDecimal quota, Boolean inTotal, String svg) {
        // 获取原资产信息
        Asset oldAsset = assetService.getAsset(assetId);
        // 检查资产是否存在
        if (oldAsset == null) {
            throw new ParamsException("资产不存在");
        }
        // 更新资产信息（仅更新非空字段）
        if (balance != null) {
            oldAsset.setBalance(balance);
        }
        if (assetName != null) {
            oldAsset.setAssetName(assetName);
        }
        if (billDate != null) {
            oldAsset.setBillDate(billDate);
        }
        if (repayDate != null) {
            oldAsset.setRepayDate(repayDate);
        }
        if (quota != null) {
            oldAsset.setQuota(quota);
        }
        if (inTotal != null) {
            oldAsset.setInTotal(inTotal);
        }
        if (svg != null) {
            oldAsset.setSvg(svg);
        }
        // 调用服务层更新资产
        assetService.updateAsset(oldAsset);
        return new Response<>(200, "修改资产成功", null);
    }

    /**
     * 获取所有资产
     * @return 所有资产列表
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "token过期"),
            @ApiResponse(responseCode = "422", description = "参数错误"),
    })
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    @Operation(summary = "获得所有资产")
    public Response<GetAllAssetResponse> getAllAsset() {
        // 从token中获取用户名
        Integer userId = Integer.parseInt(TokenValidator.getUser().get("userId"));
        // 调用服务层获取所有资产
        List<Asset> assetList = assetService.getAllAsset(userId);
        return new Response<>(200, "获得资产成功", new GetAllAssetResponse(assetList));
    }

    /**
     * 获取单个资产
     * @param id 资产ID
     * @return 资产信息
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "token过期"),
            @ApiResponse(responseCode = "422", description = "参数错误"),
    })
    @RequestMapping(value = "/asset", method = RequestMethod.GET)
    @Operation(summary = "获得资产")
    public Response<GetAssetResponse> getAsset(@RequestParam Integer id) {
        // 调用服务层获取资产
        Asset asset = assetService.getAsset(id);
        return new Response<>(200, "获得资产成功", new GetAssetResponse(asset));
    }

    /**
     * 获取日统计时间
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 日统计时间数据
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "token过期"),
            @ApiResponse(responseCode = "422", description = "参数错误"),
    })
    @RequestMapping(value = "/day-statistic", method = RequestMethod.GET)
    @Operation(summary = "获得日统计时间")
    public Response<DayStatisticTimeResponse> getDayStatistic(@RequestParam Timestamp startTime, @RequestParam Timestamp endTime) {
        // 从token中获取用户ID
        Integer userId = Integer.parseInt(TokenValidator.getUser().get("userId"));
        // 调用服务层获取日统计数据
        List<Map<String, Object>> result = assetService.getDayStatistic(userId, startTime, endTime);
        return new Response<>(200, "获得日统计时间", new DayStatisticTimeResponse(result));
    }
}