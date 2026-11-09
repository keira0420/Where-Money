package shuhuai.wheremoney.config;

import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import shuhuai.wheremoney.mapper.*;
import shuhuai.wheremoney.entity.*;
import shuhuai.wheremoney.service.ai.AiAccessGuard;
import shuhuai.wheremoney.service.excep.common.*;
import shuhuai.wheremoney.type.BillType;
import shuhuai.wheremoney.utils.TokenValidator;
import java.util.Objects;

/** Legacy form endpoints use resource ids; ownership checks always read MySQL. */
@Component
@RequiredArgsConstructor
public class ResourceAccessInterceptor implements HandlerInterceptor {
    private final AiAccessGuard access;
    private final AssetMapper assets;
    private final BillCategoryMapper categories;
    private final BudgetMapper budgets;
    private final PayBillMapper pays;
    private final IncomeBillMapper incomes;
    private final TransferBillMapper transfers;
    private final RefundBillMapper refunds;

    @Override public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();
        if (path.startsWith("/api/manage")) throw new PermissionDeniedException("日志不通过业务接口公开");
        if (path.startsWith("/api/ai/")) return true; // JSON endpoints validate in their services.
        if (TokenValidator.getUser() == null) throw new TokenExpireException("token无效");
        int userId = Integer.parseInt(TokenValidator.getUser().get("userId"));
        Integer bookId = integer(request, "bookId");
        if (bookId != null) access.requireBook(userId, bookId);
        Integer id = integer(request, "id");
        if (path.equals("/api/book/book") && id != null) access.requireBook(userId, id);
        if (path.equals("/api/asset/asset") && id != null) asset(userId, id);
        for (String key : new String[]{"assetId", "inAssetId", "outAssetId"}) {
            Integer assetId = integer(request, key);
            if (assetId != null) asset(userId, assetId);
        }
        BillType type = request.getParameter("type") == null ? null : BillType.getBillTypeEnum(request.getParameter("type"));
        if ((path.equals("/api/bill/bill") || path.equals("/api/bill/image"))) {
            Integer billId = path.endsWith("/image") ? integer(request, "billId") : id;
            if (billId == null && path.endsWith("/image")) billId = id;
            if (billId != null) {
                BaseBill bill = bill(billId, type);
                access.requireBook(userId, bill.getBookId());
                if (bookId == null) bookId = bill.getBookId();
            }
            Integer payId = integer(request, "payBillId");
            if (payId != null) {
                BaseBill pay = bill(payId, BillType.支出);
                access.requireBook(userId, pay.getBookId());
                if (bookId != null && !bookId.equals(pay.getBookId())) throw new ParamsException("退款关联账单不属于当前账本");
            }
        }
        Integer budgetId = integer(request, "budgetId");
        if (budgetId != null) {
            Budget budget = budgets.selectBudgetById(budgetId);
            if (budget == null) throw new ParamsException("预算不存在");
            access.requireBook(userId, budget.getBookId());
            bookId = budget.getBookId();
        }
        Integer categoryId = integer(request, "billCategoryId");
        if (path.equals("/api/book/bill-category") && id != null) categoryId = id;
        if (categoryId != null) {
            BillCategory category = categories.selectBillCategoryById(categoryId);
            if (category == null) throw new ParamsException("分类不存在");
            access.requireBook(userId, category.getBookId());
            if (bookId != null && !Objects.equals(bookId, category.getBookId())) throw new ParamsException("分类与账本不匹配");
            if (path.startsWith("/api/budget/") && category.getType() != BillType.支出) throw new ParamsException("只能为支出分类设置预算");
            if (type != null && category.getType() != (type == BillType.退款 ? BillType.支出 : type)) throw new ParamsException("分类类型不匹配");
        }
        return true;
    }

    private void asset(int userId, int id) {
        Asset asset = assets.selectAssetById(id);
        if (asset == null || !Objects.equals(asset.getUserId(), userId)) throw new PermissionDeniedException("无权访问该账户");
    }
    private BaseBill bill(int id, BillType type) {
        if (type == null) throw new ParamsException("账单类型不正确");
        BaseBill bill = switch (type) {
            case 支出 -> pays.selectPayBillById(id);
            case 收入 -> incomes.selectIncomeBillById(id);
            case 转账 -> transfers.selectTransferBillById(id);
            case 退款 -> refunds.selectRefundBillById(id);
        };
        if (bill == null) throw new ParamsException("账单不存在");
        return bill;
    }
    private Integer integer(HttpServletRequest request, String key) {
        String value = request.getParameter(key);
        if (value == null || value.isBlank()) return null;
        try { return Integer.valueOf(value); }
        catch (NumberFormatException e) { throw new ParamsException("无效参数: " + key); }
    }
}
