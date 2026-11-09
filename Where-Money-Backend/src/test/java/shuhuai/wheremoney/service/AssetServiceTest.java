package shuhuai.wheremoney.service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import shuhuai.wheremoney.entity.Asset;
import shuhuai.wheremoney.utils.TimeComputer;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class AssetServiceTest {
    @Resource
    private AssetService assetService;

    @Test
    public void compileTest() {
        log.info("编译测试");
    }

    @Test
    public void getAssetTest() {
        log.info("获取资产测试");
        Asset asset = assetService.getAsset(11);
        log.info(asset.toString());
    }

    @Test
    public void getDayStatisticTimeTest() {
        log.info("获取日统计时间测试");
        Timestamp startTime = null;
        Timestamp endTime = null;
        try {
            SimpleDateFormat timeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            startTime = new Timestamp(timeFormatter.parse("2022-04-01 00:00:00").getTime());
            endTime = new Timestamp(timeFormatter.parse("2022-05-05 23:59:59").getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < 10; i++) {
            long start = System.currentTimeMillis();
            List<Map<String, Object>> result = assetService.getDayStatistic(1000000000 + i, startTime, endTime);
            log.info("result: {}", result);
            assert endTime != null;
            endTime = TimeComputer.prevDay(endTime);
            long end = System.currentTimeMillis();
            log.info("耗时: {}秒", (end - start) / 1000.0);
        }
    }

    @Test
    public void changeBalanceRelativeTest() {
        log.info("更改余额相对测试");
        assetService.changeBalanceRelative(70, new BigDecimal(100));
        log.info("更改余额相对测试完成");
    }
}