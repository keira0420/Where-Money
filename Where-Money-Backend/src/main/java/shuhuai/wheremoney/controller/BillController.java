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
import org.springframework.web.multipart.MultipartFile;
import shuhuai.wheremoney.entity.*;
import shuhuai.wheremoney.response.Response;
import shuhuai.wheremoney.response.bill.*;
import shuhuai.wheremoney.service.AiBillService;
import shuhuai.wheremoney.service.AssetService;
import shuhuai.wheremoney.service.BillCategoryService;
import shuhuai.wheremoney.service.BillService;
import shuhuai.wheremoney.service.excep.common.ParamsException;
import shuhuai.wheremoney.type.BillType;
import shuhuai.wheremoney.utils.TokenValidator;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 账单管理控制器
 * 处理账单相关的HTTP请求，包括新建、修改、查询、删除账单，以及账单分类管理和统计等操作
 */
@RestController
@RequestMapping("/api/bill")
@Tag(name = "账单管理")
@Slf4j
public class BillController extends BaseController {
    /**
     * 账单服务实例，用于处理账单相关的业务逻辑
     */
    @Resource
    private BillService billService;
    /**
     * AI账单解析服务实例
     */
    @Resource
    private AiBillService aiBillService;
    /**
     * 资产服务实例，用于处理资产相关的业务逻辑
     */
    @Resource
    private AssetService assetService;
    /**
     * 账单分类服务实例，用于处理账单分类相关的业务逻辑
     */
    @jakarta.annotation.Resource
    private BillCategoryService billCategoryService;

    /**
     * 新建账单
     * @param bookId 账本ID
     * @param inAssetId 收入资产ID（可选）
     * @param outAssetId 支出资产ID（可选）
     * @param payBillId 关联的支出账单ID（可选，用于退款）
     * @param billCategoryId 账单分类ID（可选）
     * @param type 账单类型
     * @param amount 金额
     * @param transferFee 转账手续费（可选）
     * @param time 账单时间
     * @param remark 备注（可选）
     * @param refunded 是否已退款（可选，仅用于支出账单）
     * @param file 附件文件（可选）
     * @return 新建账单结果
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "token过期"),
            @ApiResponse(responseCode = "422", description = "参数错误"),
            @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    @RequestMapping(value = "/bill", method = RequestMethod.POST)
    @Operation(summary = "新建账单")
    public Response<Object> addBill(@RequestParam Integer bookId, Integer inAssetId, Integer outAssetId, Integer payBillId,
                                    Integer billCategoryId, @RequestParam BillType type, @RequestParam BigDecimal amount, BigDecimal transferFee,
                                    @RequestParam Timestamp time, String remark, Boolean refunded, MultipartFile file) {
        // 调用服务层添加账单
        billService.addBill(bookId, inAssetId, outAssetId, payBillId, billCategoryId, type, amount, transferFee, time, remark, refunded, file);
        return new Response<>(200, "新建账单成功", null);
    }

    /**
     * AI解析账单文本
     * @param bookId 账本ID
     * @param type 账单类型（支出/收入/转账）
     * @param text 待解析文本
     * @return 结构化账单草稿
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "token过期"),
            @ApiResponse(responseCode = "422", description = "参数错误或解析失败"),
            @ApiResponse(responseCode = "429", description = "请求过于频繁"),
            @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    @RequestMapping(value = "/ai/parse", method = RequestMethod.POST)
    @Operation(summary = "AI解析账单文本")
    public Response<AiParseBillResponse> parseBillByAi(@RequestParam Integer bookId, @RequestParam BillType type, @RequestParam String text) {
        Integer userId = Integer.parseInt(TokenValidator.getUser().get("userId"));
        AiParseBillResponse response = aiBillService.parseBill(userId, bookId, type, text);
        return new Response<>(200, "AI解析成功", response);
    }

    /**
     * 将账单ID转换为名称
     * @param bill 基础账单对象
     * @return 账单相关名称数组
     */
    private String[] idToString(BaseBill bill) {
        if (bill instanceof PayBill) {
            String payAsset = null;
            String billCategory = null;
            // 获取支出资产名称
            if (((PayBill) bill).getPayAssetId() != null) {
                payAsset = assetService.getAsset(((PayBill) bill).getPayAssetId()).getAssetName();
            }
            // 获取账单分类名称
            if (((PayBill) bill).getBillCategoryId() != null) {
                billCategory = billCategoryService.getBillCategory(((PayBill) bill).getBillCategoryId()).getBillCategoryName();
            }
            return new String[]{payAsset, billCategory};
        }
        if (bill instanceof IncomeBill) {
            String incomeAsset = null;
            String billCategory = null;
            // 获取收入资产名称
            if (((IncomeBill) bill).getIncomeAssetId() != null) {
                incomeAsset = assetService.getAsset(((IncomeBill) bill).getIncomeAssetId()).getAssetName();
            }
            // 获取账单分类名称
            if (((IncomeBill) bill).getBillCategoryId() != null) {
                billCategory = billCategoryService.getBillCategory(((IncomeBill) bill).getBillCategoryId()).getBillCategoryName();
            }
            return new String[]{incomeAsset, billCategory};
        }
        if (bill instanceof TransferBill) {
            String inAsset = null;
            String outAsset = null;
            // 获取转入资产名称
            if (((TransferBill) bill).getInAssetId() != null) {
                inAsset = assetService.getAsset(((TransferBill) bill).getInAssetId()).getAssetName();
            }
            // 获取转出资产名称
            if (((TransferBill) bill).getOutAssetId() != null) {
                outAsset = assetService.getAsset(((TransferBill) bill).getOutAssetId()).getAssetName();
            }
            return new String[]{inAsset, outAsset};
        }
        if (bill instanceof RefundBill) {
            String refundAsset = null;
            // 获取退款资产名称
            if (((RefundBill) bill).getRefundAssetId() != null) {
                refundAsset = assetService.getAsset(((RefundBill) bill).getRefundAssetId()).getAssetName();
            }
            return new String[]{refundAsset};
        }
        return null;
    }

    /**
     * 将账单实体转换为响应对象
     * @param bill 基础账单对象
     * @return 账单响应对象
     */
    private BaseGetBillResponse entityToResponse(BaseBill bill) {
        String[] strings = idToString(bill);
        if (bill instanceof PayBill) {
            return new GetPayBillResponse(bill, strings[0], strings[1], ((PayBill) bill).getRefunded());
        }
        if (bill instanceof IncomeBill) {
            return new GetIncomeBillResponse(bill, strings[0], strings[1]);
        }
        if (bill instanceof TransferBill) {
            return new GetTransferBillResponse(bill, strings[0], strings[1]);
        }
        if (bill instanceof RefundBill) {
            return new GetRefundBillResponse(bill, strings[0]);
        }
        return null;
    }

    /**
     * 获得账单
     * @param id 账单ID
     * @param type 账单类型
     * @return 账单信息
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "token过期"),
            @ApiResponse(responseCode = "422", description = "参数错误"),
    })
    @RequestMapping(value = "/bill", method = RequestMethod.GET)
    @Operation(summary = "获得账单")
    public Response<BaseGetBillResponse> getBill(@RequestParam Integer id, @RequestParam BillType type) {
        // 调用服务层获取账单
        BaseBill bill = billService.getBill(id, type);
        // 检查账单是否存在
        if (bill == null) {
            throw new ParamsException("账单不存在");
        }
        // 转换为响应对象并返回
        return new Response<>(200, "获得账单成功", entityToResponse(bill));
    }

    /**
     * 获得指定账本的所有账单
     * @param bookId 账本ID
     * @return 账本所有账单列表
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "token过期"),
            @ApiResponse(responseCode = "422", description = "参数错误"),
    })
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    @Operation(summary = "获得指定账本的所有账单")
    public Response<GetAllBillResponse> getBillByBook(@RequestParam Integer bookId) {
        // 调用服务层获取账本所有账单
        List<BaseBill> billList = billService.getBillByBook(bookId);
        // 转换为响应对象列表
        List<BaseGetBillResponse> billResponseList = new ArrayList<>();
        for (BaseBill bill : billList) {
            billResponseList.add(entityToResponse(bill));
        }
        return new Response<>(200, "获得指定账本的所有账单成功", new GetAllBillResponse(billResponseList));
    }

    /**
     * 获得指定账本的指定时间段的所有账单
     * @param bookId 账本ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 账本指定时间段的所有账单列表
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "token过期"),
            @ApiResponse(responseCode = "422", description = "参数错误"),
    })
    @RequestMapping(value = "/all/time", method = RequestMethod.GET)
    @Operation(summary = "获得指定账本的所有账单时间")
    public Response<GetAllBillResponse> getBillByBookTIme(@RequestParam Integer bookId, @RequestParam Timestamp startTime, @RequestParam Timestamp endTime) {
        // 调用服务层获取账本指定时间段的所有账单
        List<BaseBill> billList = billService.getBillByBookTime(bookId, startTime, endTime);
        // 转换为响应对象列表
        List<BaseGetBillResponse> billResponseList = new ArrayList<>();
        for (BaseBill bill : billList) {
            billResponseList.add(entityToResponse(bill));
        }
        return new Response<>(200, "获得指定账本的所有账单时间成功", new GetAllBillResponse(billResponseList));
    }

    /**
     * 分类统计指定账本的指定时间段的账单
     * @param bookId 账本ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 分类统计结果
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "token过期"),
            @ApiResponse(responseCode = "422", description = "参数错误"),
    })
    @RequestMapping(value = "/statistic/category", method = RequestMethod.GET)
    @Operation(summary = "分类统计指定账本的指定时间段的账单")
    public Response<StatisticResponse> getCategoryStatisticTime(@RequestParam Integer bookId, @RequestParam Timestamp startTime, @RequestParam Timestamp endTime) {
        // 调用服务层获取支出分类统计
        List<Map<String, Object>> payStatistic = billService.categoryPayStatisticTime(bookId, startTime, endTime);
        // 调用服务层获取收入分类统计
        List<Map<String, Object>> incomeStatistic = billService.categoryIncomeStatisticTime(bookId, startTime, endTime);
        return new Response<>(200, "分类统计指定账本的指定时间段的账单成功",
                new StatisticResponse(payStatistic, incomeStatistic));
    }

    /**
     * 分日统计指定账本的指定时间段的账单
     * @param bookId 账本ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 分日统计结果
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "token过期"),
            @ApiResponse(responseCode = "422", description = "参数错误"),
    })
    @RequestMapping(value = "/statistic/day", method = RequestMethod.GET)
    @Operation(summary = "分日统计指定账本的指定时间段的账单")
    public Response<StatisticResponse> getDayStatisticTime(@RequestParam Integer bookId, @RequestParam Timestamp startTime, @RequestParam Timestamp endTime) {
        // 调用服务层获取支出分日统计
        List<Map<String, Object>> payStatistic = billService.getDayPayStatisticTime(bookId, startTime, endTime);
        // 调用服务层获取收入分日统计
        List<Map<String, Object>> incomeStatistic = billService.getDayIncomeStatisticTime(bookId, startTime, endTime);
        return new Response<>(200, "分日统计指定账本的指定时间段的账单成功",
                new StatisticResponse(payStatistic, incomeStatistic));
    }

    /**
     * 添加用户自定义账单分类
     * @param bookId 账本ID
     * @param billCategoryName 分类名称
     * @param svg 分类图标
     * @param type 分类类型
     * @return 添加结果
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "token过期"),
            @ApiResponse(responseCode = "422", description = "参数错误"),
    })
    @RequestMapping(value = "/category", method = RequestMethod.POST)
    @Operation(summary = "添加用户自定义账单分类")
    public Response<StatisticResponse> addBillCategory(@RequestParam Integer bookId, @RequestParam String billCategoryName,
                                                       @RequestParam String svg, @RequestParam BillType type) {
        // 调用服务层添加账单分类
        billCategoryService.addBillCategory(bookId, billCategoryName, svg, type);
        return new Response<>(200, "添加成功", null);
    }

    /**
     * 查看账本下的所有账单分类
     * @param bookId 账本ID
     * @return 账单分类列表
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "token过期"),
            @ApiResponse(responseCode = "422", description = "参数错误"),
    })
    @RequestMapping(value = "/category", method = RequestMethod.GET)
    @Operation(summary = "查看账本下的所有账单分类")
    public Response<List<BillCategory>> getBillCategory(@RequestParam Integer bookId) {
        // 调用服务层获取账本所有账单分类
        List<BillCategory> list = billCategoryService.getBillCategoriesByBook(bookId);
        return new Response<>(200, "获取成功", list);
    }

    /**
     * 获得最大最小支出时间
     * @param bookId 账本ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 最大最小支出账单
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "token过期"),
            @ApiResponse(responseCode = "422", description = "参数错误"),
    })
    @RequestMapping(value = "/statistic/pay", method = RequestMethod.GET)
    @Operation(summary = "获得最大最小支出时间")
    public Response<MaxMinResponse> getMaxMinPayTime(@RequestParam Integer bookId, @RequestParam Timestamp startTime, @RequestParam Timestamp endTime) {
        // 调用服务层获取最大最小支出账单
        Map<String, PayBill> result = billService.getMaxMinPayBill(bookId, startTime, endTime);
        // 转换为响应对象
        GetPayBillResponse max = (GetPayBillResponse) entityToResponse(result.get("max"));
        GetPayBillResponse min = (GetPayBillResponse) entityToResponse(result.get("min"));
        return new Response<>(200, "获得最大最小支出时间成功", new MaxMinResponse(max, min));
    }

    /**
     * 获得最大最小收入时间
     * @param bookId 账本ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 最大最小收入账单
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "token过期"),
            @ApiResponse(responseCode = "422", description = "参数错误"),
    })
    @RequestMapping(value = "/statistic/income", method = RequestMethod.GET)
    @Operation(summary = "获得最大最小收入时间")
    public Response<MaxMinResponse> getMaxMinIncomeTime(@RequestParam Integer bookId, @RequestParam Timestamp startTime, @RequestParam Timestamp endTime) {
        // 调用服务层获取最大最小收入账单
        Map<String, IncomeBill> result = billService.getMaxMinIncomeBill(bookId, startTime, endTime);
        // 转换为响应对象
        GetIncomeBillResponse max = (GetIncomeBillResponse) entityToResponse(result.get("max"));
        GetIncomeBillResponse min = (GetIncomeBillResponse) entityToResponse(result.get("min"));
        return new Response<>(200, "获得最大最小收入时间成功", new MaxMinResponse(max, min));
    }

    /**
     * 获得账单图片
     * @param billId 账单ID
     * @param type 账单类型
     * @return 账单图片数据
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "token过期"),
            @ApiResponse(responseCode = "422", description = "参数错误"),
    })
    @RequestMapping(value = "/image", method = RequestMethod.GET)
    @Operation(summary = "获得账单图片")
    public Response<BillImageResponse> getBillImage(@RequestParam Integer billId, @RequestParam BillType type) {
        // 调用服务层获取账单图片
        BillImageResponse image = billService.getBillImage(billId, type);
        return new Response<>(200, "获得账单图片成功", image);
    }

    /**
     * 修改账单
     * @param id 账单ID
     * @param bookId 账本ID（可选）
     * @param amount 金额（可选）
     * @param billTime 账单时间（可选）
     * @param remark 备注（可选）
     * @param inAssetId 收入资产ID（可选）
     * @param outAssetId 支出资产ID（可选）
     * @param billCategoryId 账单分类ID（可选）
     * @param refunded 是否已退款（可选，仅用于支出账单）
     * @param type 账单类型
     * @param file 附件文件（可选）
     * @param payBillId 关联的支出账单ID（可选，用于退款）
     * @param transferFee 转账手续费（可选）
     * @return 修改账单结果
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "token过期"),
            @ApiResponse(responseCode = "422", description = "参数错误"),
    })
    @RequestMapping(value = "/bill", method = RequestMethod.PATCH)
    @Operation(summary = "修改账单")
    public Response<Object> changeBill(@RequestParam Integer id, Integer bookId, BigDecimal amount, Timestamp billTime, String remark, Integer inAssetId,
                                       Integer outAssetId, Integer billCategoryId, Boolean refunded, @RequestParam BillType type, MultipartFile file,
                                       Integer payBillId, BigDecimal transferFee) {
        // 调用服务层修改账单
        billService.changeBill(id, bookId, amount, billTime, remark, inAssetId, outAssetId, billCategoryId, refunded, type, file, payBillId, transferFee);
        return new Response<>(200, "修改账单成功", null);
    }

    /**
     * 删除账单
     * @param id 账单ID
     * @param type 账单类型
     * @return 删除账单结果
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "token过期"),
            @ApiResponse(responseCode = "422", description = "参数错误"),
    })
    @RequestMapping(value = "/bill", method = RequestMethod.DELETE)
    @Operation(summary = "删除账单")
    public Response<Object> deleteBill(@RequestParam Integer id, @RequestParam BillType type) {
        // 调用服务层删除账单
        billService.deleteBill(id, type);
        return new Response<>(200, "修改账单成功", null);
    }

    /**
     * 删除账单图片
     * @param id 账单ID
     * @param type 账单类型
     * @return 删除账单图片结果
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "token过期"),
            @ApiResponse(responseCode = "422", description = "参数错误"),
    })
    @RequestMapping(value = "/image", method = RequestMethod.DELETE)
    @Operation(summary = "删除账单图片")
    public Response<Object> deleteBillImage(@RequestParam Integer id, @RequestParam BillType type) {
        // 调用服务层删除账单图片
        billService.deleteBillImage(id, type);
        return new Response<>(200, "删除账单图片成功", null);
    }
}
