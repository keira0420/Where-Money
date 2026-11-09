package shuhuai.wheremoney.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import shuhuai.wheremoney.mapper.*;
import shuhuai.wheremoney.response.Response;
import shuhuai.wheremoney.response.ai.AiViews.*;
import shuhuai.wheremoney.response.bill.AiParseBillResponse;
import shuhuai.wheremoney.service.AiBillService;
import shuhuai.wheremoney.service.ai.*;
import shuhuai.wheremoney.type.BillType;
import shuhuai.wheremoney.utils.TokenValidator;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiAssistantController extends BaseController {
    private final AiBillService parser;
    private final AiAccessGuard access;
    private final AiConfirmationService confirmation;
    private final AiBudgetService budget;
    private final AiReportService report;
    private final AssetMapper assets;
    private final BillCategoryMapper categories;
    public record ParseRequest(Integer bookId, BillType type, String text) {}
    public record ReportRequest(Integer bookId, String month) {}

    private Integer userId() { return Integer.valueOf(TokenValidator.getUser().get("userId")); }

    @GetMapping("/context")
    public Response<?> context(@RequestParam Integer bookId, @RequestParam BillType type) {
        access.requireBook(userId(), bookId);
        return new Response<>(200, "候选数据", Map.of("assets", assets.selectAssetByUserId(userId()),
            "categories", categories.selectBillCategoryByBookType(bookId, type)));
    }

    @PostMapping("/parse")
    public Response<AiParseBillResponse> parse(@RequestBody ParseRequest request) {
        return new Response<>(200, "草稿已生成，请确认后保存",
            parser.parseBill(userId(), request.bookId(), request.type(), request.text()));
    }

    @PostMapping("/confirm")
    public Response<Confirmation> confirm(@RequestBody AiConfirmationService.Draft draft) {
        return new Response<>(200, "已保存", confirmation.confirm(userId(), draft));
    }

    @GetMapping("/budget-alerts")
    public Response<BudgetOverview> alerts(@RequestParam Integer bookId) {
        return new Response<>(200, "预算检查完成", budget.overview(userId(), bookId));
    }

    @PostMapping("/monthly-report")
    public Response<MonthlyReport> monthly(@RequestBody ReportRequest request) {
        return new Response<>(200, "月报已生成", report.generate(userId(), request.bookId(), request.month()));
    }
}
