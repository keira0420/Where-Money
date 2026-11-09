package shuhuai.wheremoney.service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import shuhuai.wheremoney.entity.BaseBill;
import shuhuai.wheremoney.entity.IncomeBill;
import shuhuai.wheremoney.entity.PayBill;
import shuhuai.wheremoney.type.BillType;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class BillServiceTest {
    @Resource
    private BillService billService;

    @Test
    public void compileTest() {
        log.info("编译测试");
    }

    @Test
    public void getBillByBookTest() {
        log.info("获取账单测试");
        List<BaseBill> bills = billService.getBillByBook(23);
        for (BaseBill bill : bills) {
            log.info(bill.toString());
        }
    }

    @Test
    public void getBillTest() {
        log.info("获取账单测试");
        PayBill bill = (PayBill) billService.getBill(181, BillType.支出);
        log.info(bill.toString());
    }

    @Test
    public void categoryStatisticTimeTest() throws ParseException {
        log.info("类别统计测试");
        Timestamp beginTime = new Timestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2022-05-01 00:00:00").getTime());
        Timestamp endTime = new Timestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2022-05-31 23:59:59").getTime());
        List<Map<String, Object>> result = billService.categoryPayStatisticTime(23, beginTime, endTime);
        for (Map<String, Object> map : result) {
            log.info(map.toString());
        }
    }

    @Test
    public void getMaxMinPayBill() throws ParseException {
        log.info("获取最大最小支出账单测试");
        Timestamp beginTime = new Timestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2022-02-01 00:00:00").getTime());
        Timestamp endTime = new Timestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2022-02-28 23:59:59").getTime());
        Map<String, PayBill> result = billService.getMaxMinPayBill(23, beginTime, endTime);
        log.info(result.toString());
    }

    @Test
    public void getMaxMinIncomeBill() throws ParseException {
        log.info("获取最大最小收入账单测试");
        Timestamp beginTime = new Timestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2022-05-01 00:00:00").getTime());
        Timestamp endTime = new Timestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2022-05-31 23:59:59").getTime());
        Map<String, IncomeBill> result = billService.getMaxMinIncomeBill(23, beginTime, endTime);
        log.info(result.toString());
    }

    @Test
    public void changeBillTest() {
        log.info("修改账单测试");
        billService.changeBill(125, null, new BigDecimal(300), null, null, null, 66, null, null,
                BillType.支出, null, null, null);
        log.info("修改成功");
    }

    @Test
    public void addBillTest() throws ParseException {
        billService.addBill(23, 7, 0, 266, 89, BillType.退款, new BigDecimal(430),
                null, new Timestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2023-05-03 19:25:00").getTime()),
                "退款", null, null);
    }

    @Test
    public void getDayIncomeStatisticTimeTest() throws ParseException {
        billService.getDayIncomeStatisticTime(23, new Timestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2023-05-01 00:00:00").getTime()),
                new Timestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2023-05-31 23:59:59").getTime()));
    }
}