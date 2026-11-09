package shuhuai.wheremoney.utils;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import shuhuai.wheremoney.entity.BillCategory;
import shuhuai.wheremoney.type.BillType;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class RedisConnectorTests {
    @Resource
    private RedisConnector redisConnector;

    @Test
    public void compileTest() {
        log.info("编译测试");
    }

    @Test
    public void writeObjectTest() {
        log.info("写入object测试");
        if (redisConnector.existObject("bill_category:76")) {
            log.info("写入object测试失败");
            return;
        }
        if (redisConnector.writeObject("bill_category:76",
                new BillCategory(76, 23, "三餐", "fluent:food-24-filled", BillType.支出), 60L)) {
            log.info("写入object测试成功");
        } else {
            log.info("写入object测试失败");
        }
    }

    @Test
    public void readObjectTest() throws InterruptedException {
        log.info("读取object测试");
        Object o = redisConnector.readObject("bill_category:76");
        if (o instanceof BillCategory) {
            log.info("读取object测试成功：" + o);
        } else {
            log.info("读取object测试失败");
        }
        Thread.sleep(60 * 1000);
        o = redisConnector.readObject("bill_category:76");
        if (o instanceof BillCategory) {
            log.info("读取object测试成功：" + o);
        } else {
            log.info("读取object测试失败");
        }
    }

    @Test
    public void writeListTest() {
        log.info("写入list测试");
        redisConnector.pushList("bill_category_list", new BillCategory(12, 23, "测试", "ceshi", BillType.支出));
    }

    @Test
    public void writeMapTest() {
        log.info("写入map测试");
        if (redisConnector.writeMap("bill_category", "76",
                new BillCategory(76, 23, "三餐", "fluent:food-24-filled", BillType.支出), 20L)) {
            log.info("写入map测试成功");
        } else {
            log.info("写入map测试失败");
        }
    }

    @Test
    public void readMapTest() {
        log.info("读取map测试");
        Object o = redisConnector.readMap("bill_category", "76");
        if (o instanceof BillCategory) {
            log.info("读取map测试成功：" + o);
        } else {
            log.info("读取map测试失败");
        }
        o = redisConnector.readMap("bill_category", "23");
        if (o instanceof BillCategory) {
            log.info("读取map测试成功：" + o);
        } else {
            log.info("读取map测试失败");
        }
    }
}
