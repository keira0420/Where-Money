package shuhuai.wheremoney.service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class BudgetServiceTest {
    @Resource
    private BudgetService budgetService;

    @Test
    public void compileTest() {
        log.info("编译测试");
    }

    @Test
    public void changeTotalUsedBudgetRelativeTest() {
        log.info("更改总使用预算测试");
        budgetService.changeTotalUsedBudgetRelative(49, new BigDecimal(100));
        log.info("更改总使用预算测试成功。");
    }
}
