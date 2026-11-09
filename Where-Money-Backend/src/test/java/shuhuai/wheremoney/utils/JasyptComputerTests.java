package shuhuai.wheremoney.utils;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class JasyptComputerTests {
    @Resource
    JasyptComputer jasyptComputer;

    @Test
    public void compileTest() {
        log.info("编译测试");
    }

    @Test
    public void jasyptEncryptTest() {
        String encryptString = jasyptComputer.encrypt("example-value-not-a-real-key");
        log.info("加密测试：" + encryptString);
        log.info("解密测试：" + jasyptComputer.decrypt(encryptString));
    }
}
