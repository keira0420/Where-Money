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
import shuhuai.wheremoney.response.Response;
import shuhuai.wheremoney.response.book.GetBookResponse;
import shuhuai.wheremoney.response.budget.GetAllBudgetResponse;
import shuhuai.wheremoney.response.budget.GetBudgetResponse;
import shuhuai.wheremoney.service.BookService;
import shuhuai.wheremoney.service.BudgetService;

import java.math.BigDecimal;

/**
 * 预算管理控制器
 * 处理预算相关的HTTP请求，包括新建、更新、查询、删除预算等操作
 */
@RestController
@RequestMapping("/api/budget")
@Tag(name = "预算管理")
@Slf4j
public class BudgetController extends BaseController {
    /**
     * 预算服务实例，用于处理预算相关的业务逻辑
     */
    @Resource
    private BudgetService budgetService;
    /**
     * 账本服务实例，用于处理账本相关的业务逻辑
     */
    @Resource
    private BookService bookService;

    /**
     * 新建预算
     * @param bookId 账本ID
     * @param billCategoryId 账单分类ID
     * @param limit 预算限额
     * @return 新建预算结果
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "token过期"),
            @ApiResponse(responseCode = "422", description = "参数错误"),
    })
    @RequestMapping(value = "/budget", method = RequestMethod.POST)
    @Operation(summary = "新建预算", description = "新建预算")
    public Response<Object> addBudget(@RequestParam Integer bookId,
                                      @RequestParam Integer billCategoryId, @RequestParam BigDecimal limit) {
        // 调用服务层添加预算（带验证）
        budgetService.addBudgetValidated(bookId, billCategoryId, limit);
        return new Response<>(200, "新建预算成功", null);
    }

    /**
     * 更新预算
     * @param budgetId 预算ID
     * @param billCategoryId 账单分类ID（可选）
     * @param limit 预算限额（可选）
     * @param amount 已用金额（可选）
     * @param times 使用次数（可选）
     * @return 更新预算结果
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "token过期"),
            @ApiResponse(responseCode = "422", description = "参数错误"),
    })
    @RequestMapping(value = "/budget", method = RequestMethod.PATCH)
    @Operation(summary = "更新预算", description = "更新预算")
    public Response<Object> updateBudget(@RequestParam Integer budgetId,
                                         Integer billCategoryId, BigDecimal limit, BigDecimal amount, Integer times) {
        // 调用服务层更新预算（带验证）
        budgetService.updateBudgetValidated(budgetId, billCategoryId, limit, amount, times);
        // 返回更新后的预算信息
        return new Response<>(200, "更新预算成功", new GetBudgetResponse(budgetService.getBudget(budgetId)));
    }

    /**
     * 查看账本全部预算
     * @param bookId 账本ID
     * @return 账本所有预算列表
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "token过期"),
            @ApiResponse(responseCode = "422", description = "参数错误"),
    })
    @RequestMapping(value = "/budget/all", method = RequestMethod.GET)
    @Operation(summary = "查看账本全部预算", description = "查看账本全部预算")
    public Response<Object> getBudgets(@RequestParam Integer bookId) {
        // 调用服务层获取账本所有预算
        return new Response<>(200, "获取预算成功", new GetAllBudgetResponse(budgetService.getBudgetsByBook(bookId)));
    }

    /**
     * 修改账本总预算
     * @param bookId 账本ID
     * @param totalBudget 总预算金额
     * @param usedBudget 已用预算金额
     * @return 修改总预算结果
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "token过期"),
            @ApiResponse(responseCode = "422", description = "参数错误"),
    })
    @RequestMapping(value = "/budget/total", method = RequestMethod.PATCH)
    @Operation(summary = "修改账本总预算", description = "修改账本总预算")
    public Response<Object> setTotalBudgets(@RequestParam Integer bookId, BigDecimal totalBudget, BigDecimal usedBudget) {
        // 调用服务层更新总预算（带验证）
        boolean changed = budgetService.updateTotalBudgetValidated(bookId, totalBudget, usedBudget);
        // 检查是否有改动
        if (!changed) {
            return new Response<>(200, "无改动", null);
        }
        // 返回更新后的账本信息
        return new Response<>(200, "修改总预算成功", new GetBookResponse(bookService.getBook(bookId)));
    }

    /**
     * 删除预算
     * @param budgetId 预算ID
     * @return 删除预算结果
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "token过期"),
            @ApiResponse(responseCode = "422", description = "参数错误"),
    })
    @RequestMapping(value = "/budget", method = RequestMethod.DELETE)
    @Operation(summary = "删除预算", description = "删除预算")
    public Response<Void> deleteBudget(@RequestParam Integer budgetId) {
        // 调用服务层删除预算
        budgetService.deleteBudget(budgetId);
        return new Response<>(200, "删除预算成功", null);
    }
}