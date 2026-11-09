package shuhuai.wheremoney.service.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import shuhuai.wheremoney.mapper.AiInsightMapper;
import shuhuai.wheremoney.response.ai.AiViews.*;
import java.sql.Timestamp;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class AiFactsService {
    private final AiInsightMapper mapper;
    private final AiAccessGuard access;

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public MonthlyFacts facts(Integer userId, Integer bookId, YearMonth month) {
        access.requireBook(userId, bookId);
        Timestamp start = Timestamp.valueOf(month.atDay(1).atStartOfDay());
        Timestamp end = Timestamp.valueOf(month.plusMonths(1).atDay(1).atStartOfDay());
        Timestamp previous = Timestamp.valueOf(month.minusMonths(1).atDay(1).atStartOfDay());
        return new MonthlyFacts(month.toString(), mapper.totals(bookId, start, end),
            mapper.totals(bookId, previous, start), mapper.topCategories(bookId, start, end));
    }
}
