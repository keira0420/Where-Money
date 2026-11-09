package shuhuai.wheremoney.ai;

import org.junit.jupiter.api.Test;
import shuhuai.wheremoney.utils.PasswordHasher;
import static org.junit.jupiter.api.Assertions.*;

class PasswordHasherTest {
    @Test void saltedPasswordAndWrongPassword() {
        String first=PasswordHasher.encode("WhereMoney2026!");
        assertNotEquals(first,PasswordHasher.encode("WhereMoney2026!"));
        assertTrue(PasswordHasher.matches("WhereMoney2026!",first));
        assertFalse(PasswordHasher.matches("wrong-password",first));
    }
    @Test void rejectsShortOrOverlongPassword() {
        assertThrows(RuntimeException.class,()->PasswordHasher.encode("123"));
        assertThrows(RuntimeException.class,()->PasswordHasher.encode("中".repeat(25)));
    }
}
