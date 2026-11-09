package shuhuai.wheremoney;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import shuhuai.wheremoney.utils.JasyptComputer;

/**
 * 应用主类
 * 用于启动WhereMoney应用程序
 */
@SpringBootApplication
@org.springframework.scheduling.annotation.EnableScheduling
@EnableEncryptableProperties
public class WhereMoneyApplication {
    /**
     * 应用主方法
     * 用于启动WhereMoney应用程序
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(WhereMoneyApplication.class, args);
    }

    /**
     * Jasypt加密器Bean
     * 用于加密和解密配置文件中的敏感信息
     *
     * @return JasyptComputer实例
     */
    @Bean("jasyptComputer")
    public StringEncryptor jasyptComputer() {
        return new JasyptComputer();
    }
}
