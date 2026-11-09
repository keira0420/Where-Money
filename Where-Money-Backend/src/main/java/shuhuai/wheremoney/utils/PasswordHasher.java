package shuhuai.wheremoney.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import shuhuai.wheremoney.service.excep.common.ParamsException;
import java.nio.charset.StandardCharsets;

public final class PasswordHasher {
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder(10);
    private PasswordHasher() {}
    public static void validate(String password) {
        if (password == null || password.length() < 8 || password.getBytes(StandardCharsets.UTF_8).length > 72)
            throw new ParamsException("密码至少8位，且UTF-8长度不超过72字节");
    }
    public static String encode(String password) { validate(password); return ENCODER.encode(password); }
    public static String upgradeLegacy(String password) { return ENCODER.encode(password); }
    public static boolean matches(String password, String hash) {
        if (password == null || hash == null) return false;
        try { return hash.startsWith("$2") ? ENCODER.matches(password, hash) : HashComputer.getHashedString(password).equals(hash); }
        catch (IllegalArgumentException e) { return false; }
    }
}
