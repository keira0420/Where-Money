package shuhuai.wheremoney.entity;

import lombok.*;
import shuhuai.wheremoney.type.BillType;

import java.io.Serializable;

/**
 * 账单分类实体类
 * 用于表示账本中的账单分类信息
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class BillCategory implements Serializable {
    private Integer id;
    private Integer bookId;
    private String billCategoryName;
    private String svg;
    private BillType type;

    /**
     * 构造方法
     *
     * @param bookId          账本ID
     * @param billCategoryName 分类名称
     * @param svg             SVG图标
     * @param type            账单类型
     */
    public BillCategory(Integer bookId, String billCategoryName, String svg, BillType type) {
        this.bookId = bookId;
        this.billCategoryName = billCategoryName;
        this.svg = svg;
        this.type = type;
    }
}