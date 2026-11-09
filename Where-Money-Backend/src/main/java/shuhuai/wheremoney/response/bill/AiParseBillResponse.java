package shuhuai.wheremoney.response.bill;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import shuhuai.wheremoney.type.BillType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * AI解析账单响应
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiParseBillResponse implements Serializable {
    private BillType type;
    private Integer billCategoryId;
    private String billCategoryName;
    private Integer inAssetId;
    private String inAssetName;
    private Integer outAssetId;
    private String outAssetName;
    private BigDecimal amount;
    private BigDecimal transferFee;
    private Timestamp billTime;
    private String remark;
}
