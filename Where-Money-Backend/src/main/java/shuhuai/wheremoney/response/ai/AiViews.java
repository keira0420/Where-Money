package shuhuai.wheremoney.response.ai;

import java.math.BigDecimal;
import java.util.List;

public final class AiViews {
    private AiViews() {}
    public record Totals(BigDecimal income, BigDecimal grossExpense, BigDecimal refund,
                         BigDecimal transferFee, long entries) {
        public BigDecimal getNetExpense() { return grossExpense.subtract(refund).add(transferFee); }
        public BigDecimal getBalance() { return income.subtract(getNetExpense()); }
    }
    public record CategoryAmount(String category, BigDecimal amount) {}
    public record BudgetAlert(String name, BigDecimal limit, BigDecimal used,
                              BigDecimal remaining, String level, String message) {}
    public record BudgetOverview(String periodStart, String periodEnd, List<BudgetAlert> items) {}
    public record MonthlyFacts(String month, Totals current, Totals previous,
                               List<CategoryAmount> topCategories) {}
    public record Narrative(String overview, List<String> observations, List<String> suggestions) {}
    public record MonthlyReport(MonthlyFacts facts, Narrative narrative, String source,
                                String warning, String generatedAt, boolean cached) {}
    public record Confirmation(String requestId, boolean alreadySaved) {}
}
