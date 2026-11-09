package shuhuai.wheremoney.ai;

import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import shuhuai.wheremoney.service.ai.*;
import shuhuai.wheremoney.service.BillService;
import shuhuai.wheremoney.type.BillType;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/** Real MySQL+Redis. Only the paid external model is stubbed and explicitly identified. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnabledIfSystemProperty(named="integration", matches="true")
class LedgerIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate db;
    @Autowired StringRedisTemplate redis;
    @Autowired AiConfirmationService confirmations;
    @Autowired BillService bills;
    @Autowired PlatformTransactionManager tx;
    @Autowired AiFactsService factsService;
    @MockitoBean AiModelGateway model;
    String token;
    int userId, bookId, assetId, otherAssetId, categoryId;

    @BeforeEach void fixture() throws Exception {
        String user = "test_" + UUID.randomUUID();
        assertEquals(200, form("user/register", "POST", null, Map.of("userName",user,"password","Integration123!")).path("code").asInt());
        token = form("user/login", "POST", null, Map.of("userName",user,"password","Integration123!")).path("data").path("token").asText();
        bookId = form("book/all", "GET", token, Map.of()).path("data").path("bookList").get(0).path("id").asInt();
        userId = db.queryForObject("select user_id from book where id=?", Integer.class, bookId);
        for (String name : List.of("支付宝", "银行卡")) {
            assertEquals(200, form("asset/asset", "POST", token, Map.of("assetName",name,"balance","10000","type","资金","inTotal","true","svg","wallet")).path("code").asInt());
        }
        assetId = db.queryForObject("select id from asset where user_id=? and asset_name='支付宝'",Integer.class,userId);
        otherAssetId = db.queryForObject("select id from asset where user_id=? and asset_name='银行卡'",Integer.class,userId);
        categoryId = db.queryForObject("select min(id) from bill_category where book_id=? and type='支出'",Integer.class,bookId);
        when(model.complete(anyString(),anyString())).thenReturn("{\"status\":\"SUCCESS\",\"amount\":\"128.00\",\"assetName\":\"支付宝\",\"billCategoryName\":\"" + db.queryForObject("select bill_category_name from bill_category where id=?",String.class,categoryId) + "\"}");
    }

    @Test void parseAndConfirmOnce() throws Exception {
        JsonNode parsed=postJson("ai/parse",Map.of("bookId",bookId,"type","支出","text","支付宝吃饭128元"));
        assertEquals(200,parsed.path("code").asInt());
        assertEquals(assetId,parsed.path("data").path("outAssetId").asInt());
        assertEquals(0,payCount()); // Parsing must never persist a bill.
        verify(model).complete(contains("支付宝"),eq("支付宝吃饭128元"));
        var draft=draft(UUID.randomUUID().toString(),"128.00");
        assertFalse(confirmations.confirm(userId,draft).alreadySaved());
        assertTrue(confirmations.confirm(userId,draft).alreadySaved());
        assertEquals(1,payCount()); assertEquals(0,balance().compareTo(new BigDecimal("9872")));
    }

    @Test void concurrentIdempotency() throws Exception {
        var draft=draft(UUID.randomUUID().toString(),"128.00");
        try(var pool=Executors.newVirtualThreadPerTaskExecutor()) {
            var futures=new ArrayList<Future<?>>();
            for(int i=0;i<4;i++) futures.add(pool.submit(()->confirmations.confirm(userId,draft)));
            for(var future:futures) future.get(15,TimeUnit.SECONDS);
        }
        assertEquals(1,payCount()); assertEquals(0,balance().compareTo(new BigDecimal("9872")));
    }

    @Test void sameIdDifferentPayloadRejected() {
        String id=UUID.randomUUID().toString();
        confirmations.confirm(userId,draft(id,"128.00"));
        assertThrows(RuntimeException.class,()->confirmations.confirm(userId,draft(id,"129.00")));
        assertEquals(1,payCount());
    }

    @Test void transactionRollbackKeepsDatabaseAndCache() {
        String key="asset:"+assetId;
        redis.opsForValue().set(key,"before-transaction");
        int pendingBefore=db.queryForObject("select count(*) from cache_eviction_task where cache_key=?",Integer.class,key);
        assertThrows(IllegalStateException.class,()->new TransactionTemplate(tx).execute(status->{
            bills.addBill(bookId,null,assetId,null,categoryId,BillType.支出,new BigDecimal("128"),null,Timestamp.valueOf(LocalDateTime.now()),"rollback",false,null);
            throw new IllegalStateException("force rollback");
        }));
        assertEquals(0,payCount()); assertEquals(0,balance().compareTo(new BigDecimal("10000")));
        assertEquals("before-transaction",redis.opsForValue().get(key));
        assertEquals(pendingBefore,db.queryForObject("select count(*) from cache_eviction_task where cache_key=?",Integer.class,key));
        redis.delete(key);
    }

    @Test void commitInvalidatesCachedBalance() {
        String key="asset:"+assetId;
        redis.opsForValue().set(key,"old-value");
        confirmations.confirm(userId,draft(UUID.randomUUID().toString(),"128.00"));
        assertNull(redis.opsForValue().get(key));
        assertTrue(db.queryForObject("select count(*) from cache_eviction_task where cache_key=?",Integer.class,key)>0);
    }

    @Test void rejectsHallucinatedAccountAndInvalidJson() throws Exception {
        when(model.complete(anyString(),anyString())).thenReturn("{\"status\":\"SUCCESS\",\"amount\":\"128\",\"assetName\":\"不存在的账户\",\"billCategoryName\":\"餐饮\"}");
        assertEquals(422,postJson("ai/parse",Map.of("bookId",bookId,"type","支出","text","吃饭128")).path("code").asInt());
        when(model.complete(anyString(),anyString())).thenReturn("not json");
        assertEquals(500,postJson("ai/parse",Map.of("bookId",bookId,"type","支出","text","吃饭128")).path("code").asInt());
        assertEquals(0,payCount());
    }

    @Test void rejectsNegativeAndExcessPrecision() {
        assertThrows(RuntimeException.class,()->confirmations.confirm(userId,draft(UUID.randomUUID().toString(),"-1")));
        assertThrows(RuntimeException.class,()->confirmations.confirm(userId,draft(UUID.randomUUID().toString(),"1.001")));
        assertEquals(0,payCount());
    }

    @Test void ownershipAndMissingToken() throws Exception {
        assertEquals(401,form("book/all","GET",null,Map.of()).path("code").asInt());
        String username="other_"+UUID.randomUUID();
        form("user/register","POST",null,Map.of("userName",username,"password","Integration123!"));
        String otherToken=form("user/login","POST",null,Map.of("userName",username,"password","Integration123!")).path("data").path("token").asText();
        assertEquals(403,form("asset/asset","GET",otherToken,Map.of("id",String.valueOf(assetId))).path("code").asInt());
        assertEquals(403,form("bill/all","GET",otherToken,Map.of("bookId",String.valueOf(bookId))).path("code").asInt());
        assertEquals(403,form("ai/context","GET",otherToken,Map.of("bookId",String.valueOf(bookId),"type","支出")).path("code").asInt());
    }

    @Test void budgetWarningAndMonthlyCacheChangesWithFacts() throws Exception {
        form("budget/budget/total","PATCH",token,Map.of("bookId",String.valueOf(bookId),"totalBudget","150"));
        confirmations.confirm(userId,draft(UUID.randomUUID().toString(),"128.00"));
        JsonNode alerts=form("ai/budget-alerts","GET",token,Map.of("bookId",String.valueOf(bookId)));
        assertEquals("WARNING",alerts.path("data").path("items").get(0).path("level").asText());
        when(model.complete(anyString(),anyString())).thenReturn("{\"overview\":\"本月消费概览\",\"observations\":[\"关注主要消费分类\"],\"suggestions\":[\"检查非必要消费\"]}");
        var payload=Map.of("bookId",bookId,"month",YearMonth.now().toString());
        JsonNode first=postJson("ai/monthly-report",payload);
        assertEquals("AI",first.path("data").path("source").asText());
        assertEquals(128,first.path("data").path("facts").path("current").path("netExpense").asInt());
        assertTrue(postJson("ai/monthly-report",payload).path("data").path("cached").asBoolean());
        confirmations.confirm(userId,draft(UUID.randomUUID().toString(),"30.00"));
        assertFalse(postJson("ai/monthly-report",payload).path("data").path("cached").asBoolean());
        assertEquals("EXCEEDED",form("ai/budget-alerts","GET",token,Map.of("bookId",String.valueOf(bookId))).path("data").path("items").get(0).path("level").asText());
    }

    @Test void modelFailureIsExplicitFallback() throws Exception {
        confirmations.confirm(userId,draft(UUID.randomUUID().toString(),"128.00"));
        when(model.complete(anyString(),anyString())).thenThrow(new RuntimeException("offline"));
        JsonNode report=postJson("ai/monthly-report",Map.of("bookId",bookId,"month",YearMonth.now().toString())).path("data");
        assertEquals("RULES",report.path("source").asText());
        assertFalse(report.path("warning").asText().isBlank());
        assertEquals(128,report.path("facts").path("current").path("netExpense").asInt());
    }

    private AiConfirmationService.Draft draft(String id,String amount) {
        return new AiConfirmationService.Draft(id,bookId,BillType.支出,new BigDecimal(amount),null,
            Timestamp.valueOf(LocalDateTime.now().withNano(0)),"integration",assetId,null,categoryId);
    }

    @Test void incomeTransferFeeAndRefundHaveCorrectCashFlow() {
        confirmations.confirm(userId,draft(UUID.randomUUID().toString(),"128.00"));
        int incomeCategory=db.queryForObject("select min(id) from bill_category where book_id=? and type='收入'",Integer.class,bookId);
        var time=Timestamp.valueOf(LocalDateTime.now().withNano(0));
        confirmations.confirm(userId,new AiConfirmationService.Draft(UUID.randomUUID().toString(),bookId,BillType.收入,
            new BigDecimal("5000"),null,time,"income",null,assetId,incomeCategory));
        confirmations.confirm(userId,new AiConfirmationService.Draft(UUID.randomUUID().toString(),bookId,BillType.转账,
            new BigDecimal("100"),new BigDecimal("2"),time,"transfer",assetId,otherAssetId,null));
        int payId=db.queryForObject("select id from pay_bill where book_id=?",Integer.class,bookId);
        bills.addBill(bookId,assetId,null,payId,null,BillType.退款,new BigDecimal("20"),null,time,"refund",false,null);
        var facts=factsService.facts(userId,bookId,YearMonth.now());
        assertEquals(0,facts.current().income().compareTo(new BigDecimal("5000")));
        assertEquals(0,facts.current().getNetExpense().compareTo(new BigDecimal("110")));
        assertEquals(0,facts.current().getBalance().compareTo(new BigDecimal("4890")));
        BigDecimal total=db.queryForObject("select sum(balance) from asset where user_id=?",BigDecimal.class,userId);
        assertEquals(0,total.compareTo(new BigDecimal("24890")));
    }

    @Test void concurrentDistinctBillsDoNotLoseBudgetOrBalance() throws Exception {
        var drafts=java.util.stream.IntStream.range(0,4).mapToObj(i->draft(UUID.randomUUID().toString(),"10.00")).toList();
        try(var pool=Executors.newVirtualThreadPerTaskExecutor()) {
            var futures=new ArrayList<Future<?>>();
            for(var draft:drafts) futures.add(pool.submit(()->confirmations.confirm(userId,draft)));
            for(var future:futures) future.get(15,TimeUnit.SECONDS);
        }
        assertEquals(4,payCount()); assertEquals(0,balance().compareTo(new BigDecimal("9960")));
        BigDecimal used=db.queryForObject("select used_budget from book where id=?",BigDecimal.class,bookId);
        assertEquals(0,used.compareTo(new BigDecimal("40")));
    }
    private int payCount(){return db.queryForObject("select count(*) from pay_bill where book_id=?",Integer.class,bookId);}
    private BigDecimal balance(){return db.queryForObject("select balance from asset where id=?",BigDecimal.class,assetId);}
    private JsonNode postJson(String path,Object value) throws Exception {
        return json.readTree(mvc.perform(post("/api/"+path).header("Authorization","Bearer "+token).contentType("application/json").content(json.writeValueAsBytes(value))).andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8));
    }
    private JsonNode form(String path,String method,String auth,Map<String,String> values) throws Exception {
        var builder=request(org.springframework.http.HttpMethod.valueOf(method),"/api/"+path);
        if(auth!=null)builder.header("Authorization","Bearer "+auth);
        values.forEach(builder::param);
        return json.readTree(mvc.perform(builder).andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8));
    }
}
