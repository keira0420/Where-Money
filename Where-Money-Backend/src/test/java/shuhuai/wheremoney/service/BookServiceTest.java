package shuhuai.wheremoney.service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class BookServiceTest {
    @Resource
    private BookService bookService;

    @Test
    public void compileTest() {
        log.info("编译测试");
    }

    @Test
    public void getPayMonthTest() throws ParseException {
        log.info("获取月支出测试");
        Timestamp time = new Timestamp(new SimpleDateFormat("yyyy-MM").parse("2022-06").getTime());
        log.info("{}号账本月支出：{}", 23, bookService.getPayMonth(23, time));
    }

    @Test
    public void getIncomeMonthTest() throws ParseException {
        log.info("获取月收入测试");
        Timestamp time = new Timestamp(new SimpleDateFormat("yyyy-MM").parse("2022-06").getTime());
        log.info("{}号账本月收入：{}", 23, bookService.getIncomeMonth(23, time));
    }

    @Test
    public void getBalanceMonthTest() throws ParseException {
        log.info("获取月结余测试");
        Timestamp time = new Timestamp(new SimpleDateFormat("yyyy-MM").parse("2022-06").getTime());
        log.info("{}号账本月结余：{}", 23, bookService.getBalanceMonth(23, time));
    }

    @Test
    public void getRefundMonthTest() throws ParseException {
        log.info("获取月退款测试");
        Timestamp time = new Timestamp(new SimpleDateFormat("yyyy-MM").parse("2022-06").getTime());
        log.info("{}号账本月退款：{}", 23, bookService.getRefundMonth(23, time));
    }
}