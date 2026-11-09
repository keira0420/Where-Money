package shuhuai.wheremoney.utils;

import lombok.NoArgsConstructor;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.beans.factory.annotation.Value;

/**
 * Jasypt加密工具类
 * 实现StringEncryptor接口，用于字符串的加密和解密
 * 使用PooledPBEStringEncryptor进行加密操作
 */
@NoArgsConstructor
public class JasyptComputer implements StringEncryptor {
    /**
     * 加密密码
     * 从配置文件中读取
     */
    @Value("${jasypt.encryptor.password}")
    private String password;
    /**
     * 加密算法
     * 从配置文件中读取
     */
    @Value("${jasypt.encryptor.algorithm}")
    private String algorithm;

    /**
     * 加密字符串
     *
     * @param message 待加密的字符串
     * @return 加密后的字符串
     */
    @Override
    public String encrypt(String message) {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        encryptor.setConfig(getConfig());
        return encryptor.encrypt(message);
    }

    /**
     * 解密字符串
     *
     * @param encryptedMessage 待解密的字符串
     * @return 解密后的字符串
     */
    @Override
    public String decrypt(String encryptedMessage) {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        encryptor.setConfig(getConfig());
        return encryptor.decrypt(encryptedMessage);
    }

    /**
     * 获取加密配置
     *
     * @return 加密配置对象
     */
    public SimpleStringPBEConfig getConfig() {
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(this.password);
        config.setAlgorithm(this.algorithm);
        config.setKeyObtentionIterations(1000);
        config.setPoolSize(1);
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setIvGeneratorClassName("org.jasypt.iv.NoIvGenerator");
        config.setStringOutputType("base64");
        return config;
    }
}