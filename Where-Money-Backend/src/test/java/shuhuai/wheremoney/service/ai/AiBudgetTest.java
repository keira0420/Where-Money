package shuhuai.wheremoney.service.ai;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class AiBudgetTest {
    @Test void warningBoundary() {
        assertEquals("NORMAL",level("79.99"));
        assertEquals("WARNING",level("80"));
        assertEquals("REACHED",level("100"));
        assertEquals("EXCEEDED",level("100.01"));
    }
    @Test void zeroBudgetAndUnsetBudgetAreDifferent() {
        assertEquals("UNSET",AiBudgetService.alert("总预算",null,BigDecimal.ZERO).level());
        assertEquals("REACHED",AiBudgetService.alert("总预算",BigDecimal.ZERO,BigDecimal.ZERO).level());
        assertEquals("EXCEEDED",AiBudgetService.alert("总预算",BigDecimal.ZERO,BigDecimal.ONE).level());
    }
    private String level(String used) {return AiBudgetService.alert("分类",new BigDecimal("100"),new BigDecimal(used)).level();}
}
