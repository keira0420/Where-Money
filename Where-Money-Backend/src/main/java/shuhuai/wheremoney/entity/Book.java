package shuhuai.wheremoney.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 账本实体类
 * 用于表示用户的账本信息
 */
@Setter
@Getter
@AllArgsConstructor
public class Book implements Serializable {
    private Integer id;
    private Integer userId;
    private String title;
    private Timestamp createTime;
    private Integer beginDate;
    private BigDecimal totalBudget;
    private BigDecimal usedBudget;

    /**
     * 构造方法
     *
     * @param userId    用户ID
     * @param title     账本标题
     * @param beginDate 月初日期
     */
    public Book(Integer userId, String title, Integer beginDate) {
        this.userId = userId;
        this.title = title;
        this.beginDate = beginDate;
    }

}