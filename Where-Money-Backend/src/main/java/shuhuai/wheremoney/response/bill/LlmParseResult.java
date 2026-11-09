package shuhuai.wheremoney.response.bill;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LlmParseResult {
    private BigDecimal amount;
    private BigDecimal transferFee;
    private Timestamp billTime;
    private String remark;
    private String assetName;
    private String billCategoryName;
    private String outAssetName;
    private String inAssetName;
}
