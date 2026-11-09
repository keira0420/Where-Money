package shuhuai.wheremoney.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import shuhuai.wheremoney.type.BillType;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * AI账单解析日志
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AiBillParseLog implements Serializable {
    private Integer id;
    private Integer userId;
    private Integer bookId;
    private BillType type;
    private String inputText;
    private String modelName;
    private String status;
    private String errorCode;
    private String errorMessage;
    private String llmRawJson;
    private Long latencyMs;
    private Timestamp createTime;
}
