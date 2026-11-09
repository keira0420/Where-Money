package shuhuai.wheremoney.type;

import lombok.Getter;

/**
 * 账单类型枚举
 * 定义了系统支持的账单类型
 */
@Getter
public enum BillType {
    /**
     * 收入账单
     */
    收入("收入"),
    /**
     * 支出账单
     */
    支出("支出"),
    /**
     * 转账账单
     */
    转账("转账"),
    /**
     * 退款账单
     */
    退款("退款");

    /**
     * 账单类型名称
     */
    private String type;

    /**
     * 构造方法
     *
     * @param type 账单类型名称
     */
    BillType(String type) {
        this.type = type;
    }

    /**
     * 根据类型名称获取账单类型枚举
     *
     * @param type 账单类型名称
     * @return 对应的账单类型枚举
     */
    public static BillType getBillTypeEnum(String type) {
        return switch (type) {
            case "收入" -> 收入;
            case "支出" -> 支出;
            case "转账" -> 转账;
            case "退款" -> 退款;
            default -> null;
        };
    }

    /**
     * 设置账单类型名称
     *
     * @param type 账单类型名称
     */
    public void setType(String type) {
        this.type = type;
    }
}