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
import shuhuai.wheremoney.response.book.GetAllBillCategoryResponse;
import shuhuai.wheremoney.response.book.GetAllBookResponse;
import shuhuai.wheremoney.response.book.GetBookResponse;
import shuhuai.wheremoney.response.book.StatisticAmountResponse;
import shuhuai.wheremoney.service.BookService;
import shuhuai.wheremoney.type.BillType;
import shuhuai.wheremoney.utils.TokenValidator;

import java.sql.Timestamp;

/**
 * 账本管理控制器
 * 处理账本相关的HTTP请求，包括新建、查询、删除账本，以及账本分类管理和统计等操作
 */
@RestController
@RequestMapping("/api/book")
@Tag(name = "账本管理")
@Slf4j
public class BookController extends BaseController {
    /**
     * 账本服务实例，用于处理账本相关的业务逻辑
     */
    @Resource
    private BookService bookService;

    /**
     * 新建账本
     * @param title 账本标题
     * @param beginDate 开始日期
     * @return 新建账本结果
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "token过期"),
            @ApiResponse(responseCode = "422", description = "参数错误"),
            @ApiResponse(responseCode = "400", description = "标题已被占用"),
            @ApiResponse(responseCode = "500", description = "服务器错误")
    })
    @RequestMapping(value = "/book", method = RequestMethod.POST)
    @Operation(summary = "新建账本")
    public Response<Object> addBook(@RequestParam String title, @RequestParam Integer beginDate) {
        // 从token中获取用户名
        Integer userId = Integer.parseInt(TokenValidator.getUser().get("userId"));
        // 调用服务层添加账本
        bookService.addBook(userId, title, beginDate);
        return new Response<>(200, "新建账本成功", null);
    }

    /**
     * 获得用户所有账本
     * @return 用户所有账本列表
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "token过期"),
            @ApiResponse(responseCode = "422", description = "参数错误"),
            @ApiResponse(responseCode = "404", description = "用户不存在")
    })
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    @Operation(summary = "获得账本")
    public Response<GetAllBookResponse> getBook() {
        // 从token中获取用户名
        Integer userId = Integer.parseInt(TokenValidator.getUser().get("userId"));
        // 调用服务层获取用户所有账本
        return new Response<>(200, "获得账本成功", new GetAllBookResponse(bookService.getBookList(userId)));
    }

    /**
     * 获得指定账本
     * @param id 账本ID
     * @return 账本信息
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "token过期"),
            @ApiResponse(responseCode = "422", description = "参数错误"),
            @ApiResponse(responseCode = "404", description = "用户不存在")
    })
    @RequestMapping(value = "/book", method = RequestMethod.GET)
    @Operation(summary = "获得账本")
    public Response<GetBookResponse> getBook(@RequestParam Integer id) {
        // 调用服务层获取指定账本
        return new Response<>(200, "获得账本成功", new GetBookResponse(bookService.getBook(id)));
    }

    /**
     * 获得账本月支出
     * @param bookId 账本ID
     * @param month 月份
     * @return 账本月支出金额
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "token过期"),
            @ApiResponse(responseCode = "422", description = "参数错误"),
    })
    @RequestMapping(value = "/pay-month", method = RequestMethod.GET)
    @Operation(summary = "获得账本月支出")
    public Response<Object> getPayMonth(@RequestParam Integer bookId, @RequestParam Timestamp month) {
        // 调用服务层获取账本月支出
        return new Response<>(200, "获得账本月支出成功", new StatisticAmountResponse(bookService.getPayMonth(bookId, month)));
    }

    /**
     * 获得账本月收入
     * @param bookId 账本ID
     * @param month 月份
     * @return 账本月收入金额
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "token过期"),
            @ApiResponse(responseCode = "422", description = "参数错误"),
    })
    @RequestMapping(value = "/income-month", method = RequestMethod.GET)
    @Operation(summary = "获得账本月收入")
    public Response<Object> getIncomeMonth(@RequestParam Integer bookId, @RequestParam Timestamp month) {
        // 调用服务层获取账本月收入
        return new Response<>(200, "获得账本月收入成功", new StatisticAmountResponse(bookService.getIncomeMonth(bookId, month)));
    }

    /**
     * 获得账本月结余
     * @param bookId 账本ID
     * @param month 月份
     * @return 账本月结余金额
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "token过期"),
            @ApiResponse(responseCode = "422", description = "参数错误"),
    })
    @RequestMapping(value = "/balance-month", method = RequestMethod.GET)
    @Operation(summary = "获得账本月结余")
    public Response<Object> getBalanceMonth(@RequestParam Integer bookId, @RequestParam Timestamp month) {
        // 调用服务层获取账本月结余
        return new Response<>(200, "获得账本月结余成功", new StatisticAmountResponse(bookService.getBalanceMonth(bookId, month)));
    }

    /**
     * 获得账本月退款
     * @param bookId 账本ID
     * @param month 月份
     * @return 账本月退款金额
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "token过期"),
            @ApiResponse(responseCode = "422", description = "参数错误"),
    })
    @RequestMapping(value = "/refund-month", method = RequestMethod.GET)
    @Operation(summary = "获得账本月退款")
    public Response<Object> getRefundMonth(@RequestParam Integer bookId, @RequestParam Timestamp month) {
        // 调用服务层获取账本月退款
        return new Response<>(200, "获得账本月退款成功", new StatisticAmountResponse(bookService.getRefundMonth(bookId, month)));
    }

    /**
     * 获得所有账单分类
     * @param bookId 账本ID
     * @param type 账单类型
     * @return 账单分类列表
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "token过期"),
            @ApiResponse(responseCode = "422", description = "参数错误"),
    })
    @RequestMapping(value = "/bill-category", method = RequestMethod.GET)
    @Operation(summary = "获得所有账单分类")
    public Response<Object> getAllBillCategory(@RequestParam Integer bookId, @RequestParam BillType type) {
        // 调用服务层获取所有账单分类
        return new Response<>(200, "获得所有账单分类", new GetAllBillCategoryResponse(bookService.getAllBillCategory(bookId, type)));
    }

    /**
     * 新建账单分类
     * @param bookId 账本ID
     * @param billCategoryName 分类名称
     * @param svg 分类图标（可选）
     * @param type 分类类型
     * @return 新建账单分类结果
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "token过期"),
            @ApiResponse(responseCode = "422", description = "参数错误"),
    })
    @RequestMapping(value = "/bill-category", method = RequestMethod.POST)
    @Operation(summary = "新建账单分类")
    public Response<Object> addBillCategory(@RequestParam Integer bookId, @RequestParam String billCategoryName,
                                            @RequestParam(required = false) String svg, @RequestParam BillType type) {
        // 调用服务层添加账单分类
        bookService.addBillCategory(bookId, billCategoryName, svg, type);
        return new Response<>(200, "新建账单分类成功", null);
    }

    /**
     * 删除账单分类
     * @param id 分类ID
     * @return 删除账单分类结果
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "token过期"),
            @ApiResponse(responseCode = "422", description = "参数错误"),
    })
    @RequestMapping(value = "/bill-category", method = RequestMethod.DELETE)
    @Operation(summary = "删除账单分类")
    public Response<Object> deleteBillCategory(@RequestParam Integer id) {
        // 调用服务层删除账单分类
        bookService.deleteBillCategory(id);
        return new Response<>(200, "删除账单分类成功", null);
    }

    /**
     * 更新账单分类
     * @param id 分类ID
     * @param billCategoryName 分类名称（可选）
     * @param svg 分类图标（可选）
     * @param type 分类类型（可选）
     * @param bookId 账本ID（可选）
     * @return 更新账单分类结果
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "token过期"),
            @ApiResponse(responseCode = "422", description = "参数错误"),
    })
    @RequestMapping(value = "/bill-category", method = RequestMethod.PUT)
    @Operation(summary = "更新账单分类")
    public Response<Object> updateBillCategory(@RequestParam Integer id, @RequestParam(required = false) String billCategoryName,
                                               @RequestParam(required = false) String svg,
                                               @RequestParam(required = false) BillType type,
                                               @RequestParam(required = false) Integer bookId) {
        // 调用服务层更新账单分类
        bookService.updateBillCategory(id, billCategoryName, svg, type, bookId);
        return new Response<>(200, "更新账单分类成功", null);
    }

    /**
     * 删除账本
     * @param id 账本ID
     * @return 删除账本结果
     */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "token过期"),
            @ApiResponse(responseCode = "422", description = "参数错误"),
    })
    @RequestMapping(value = "/book", method = RequestMethod.DELETE)
    @Operation(summary = "删除账本")
    public Response<Object> deleteBook(@RequestParam Integer id) {
        // 从token中获取用户名
        Integer userId = Integer.parseInt(TokenValidator.getUser().get("userId"));
        // 调用服务层删除账本
        bookService.deleteBook(id, userId);
        return new Response<>(200, "删除账本成功", null);
    }
}