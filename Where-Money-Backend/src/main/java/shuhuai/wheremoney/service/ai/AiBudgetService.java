package shuhuai.wheremoney.service.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shuhuai.wheremoney.entity.*;
import shuhuai.wheremoney.mapper.BookMapper;
import shuhuai.wheremoney.mapper.BillCategoryMapper;
import shuhuai.wheremoney.response.ai.AiViews.*;
import shuhuai.wheremoney.service.BudgetService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class AiBudgetService {
    private final AiAccessGuard access;
    private final BudgetService budgets;
    private final BookMapper books;
    private final BillCategoryMapper categories;

    public BudgetOverview overview(Integer userId, Integer bookId) {
        Book book = access.requireBook(userId, bookId);
        var categoryBudgets = budgets.getBudgetsByBook(bookId); // Rebuild from authoritative bills.
        book = books.selectBookById(bookId);
        var items = new ArrayList<BudgetAlert>();
        items.add(alert("总预算", book.getTotalBudget(), book.getUsedBudget()));
        for (Budget budget : categoryBudgets) {
            BillCategory category = categories.selectBillCategoryById(budget.getBillCategoryId());
            items.add(alert(category == null ? "已删除分类" : category.getBillCategoryName(),
                budget.getLimit(), budget.getUsed()));
        }
        LocalDate now = LocalDate.now(ZoneId.of("Asia/Shanghai"));
        int begin = book.getBeginDate();
        LocalDate start = now.withDayOfMonth(begin);
        if (now.isBefore(start)) start = start.minusMonths(1);
        return new BudgetOverview(start.toString(), start.plusMonths(1).toString(), items);
    }

    static BudgetAlert alert(String name, BigDecimal limit, BigDecimal used) {
        used = used == null ? BigDecimal.ZERO : used;
        if (limit == null) return new BudgetAlert(name, null, used, null, "UNSET", "尚未设置预算");
        BigDecimal remaining = limit.subtract(used);
        String level = used.compareTo(limit) > 0 ? "EXCEEDED"
            : used.compareTo(limit) == 0 ? "REACHED"
            : used.compareTo(limit.multiply(new BigDecimal("0.8"))) >= 0 ? "WARNING" : "NORMAL";
        String message = switch (level) {
            case "EXCEEDED" -> "已超支 ¥" + remaining.abs().toPlainString();
            case "REACHED" -> "预算已用完";
            case "WARNING" -> "预算使用已达80%，请关注后续消费";
            default -> "预算使用正常";
        };
        return new BudgetAlert(name, limit, used, remaining, level, message);
    }
}
