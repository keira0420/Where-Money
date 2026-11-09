package shuhuai.wheremoney.mapper;

import org.apache.ibatis.annotations.*;
import shuhuai.wheremoney.response.ai.AiViews.*;
import java.sql.Timestamp;
import java.util.List;

@Mapper
public interface AiInsightMapper {
    // Cash-flow basis: refund belongs to its actual refund month; transfer principal is excluded.
    @Select("""
        SELECT COALESCE(SUM(income),0) income, COALESCE(SUM(expense),0) grossExpense,
               COALESCE(SUM(refund),0) refund, COALESCE(SUM(fee),0) transferFee,
               COUNT(*) entries FROM (
          SELECT amount income, 0 expense, 0 refund, 0 fee FROM income_bill
          WHERE book_id=#{bookId} AND bill_time >= #{start} AND bill_time < #{end}
          UNION ALL SELECT 0, amount, 0, 0 FROM pay_bill
          WHERE book_id=#{bookId} AND bill_time >= #{start} AND bill_time < #{end}
          UNION ALL SELECT 0, 0, amount, 0 FROM refund_bill
          WHERE book_id=#{bookId} AND bill_time >= #{start} AND bill_time < #{end}
          UNION ALL SELECT 0, 0, 0, COALESCE(transfer_fee,0) FROM transfer_bill
          WHERE book_id=#{bookId} AND bill_time >= #{start} AND bill_time < #{end}
        ) movements
        """)
    Totals totals(@Param("bookId") Integer bookId, @Param("start") Timestamp start, @Param("end") Timestamp end);

    @Select("""
        SELECT c.bill_category_name category, SUM(p.amount) amount
        FROM pay_bill p JOIN bill_category c ON c.id=p.bill_category_id
        WHERE p.book_id=#{bookId} AND p.bill_time >= #{start} AND p.bill_time < #{end}
        GROUP BY c.id,c.bill_category_name ORDER BY amount DESC,c.id LIMIT 5
        """)
    List<CategoryAmount> topCategories(@Param("bookId") Integer bookId, @Param("start") Timestamp start,
                                       @Param("end") Timestamp end);

    @Insert("""
        INSERT INTO ai_bill_confirmation(user_id, request_id, payload_hash, saved)
        VALUES(#{userId},#{requestId},#{hash},0)
        ON DUPLICATE KEY UPDATE payload_hash=payload_hash
        """)
    int claim(@Param("userId") Integer userId, @Param("requestId") String requestId, @Param("hash") String hash);

    @Select("SELECT payload_hash FROM ai_bill_confirmation WHERE user_id=#{userId} AND request_id=#{requestId} FOR UPDATE")
    String confirmationHash(@Param("userId") Integer userId, @Param("requestId") String requestId);

    @Select("SELECT saved FROM ai_bill_confirmation WHERE user_id=#{userId} AND request_id=#{requestId} FOR UPDATE")
    boolean confirmationSaved(@Param("userId") Integer userId, @Param("requestId") String requestId);

    @Update("UPDATE ai_bill_confirmation SET saved=1 WHERE user_id=#{userId} AND request_id=#{requestId}")
    void markSaved(@Param("userId") Integer userId, @Param("requestId") String requestId);

    @Insert("""
        INSERT INTO ai_report_log(user_id,book_id,report_month,source,latency_ms)
        VALUES(#{userId},#{bookId},#{month},#{source},#{latency})
        """)
    void logReport(@Param("userId") Integer userId, @Param("bookId") Integer bookId,
                   @Param("month") String month, @Param("source") String source, @Param("latency") long latency);
}
