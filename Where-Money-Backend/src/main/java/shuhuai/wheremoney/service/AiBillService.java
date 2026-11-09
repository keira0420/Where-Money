package shuhuai.wheremoney.service;

import shuhuai.wheremoney.response.bill.AiParseBillResponse;
import shuhuai.wheremoney.type.BillType;

/**
 * AI账单解析服务
 */
public interface AiBillService {
    /**
     * 解析自然语言账单文本
     *
     * @param userId 用户ID
     * @param bookId 账本ID
     * @param type 账单类型
     * @param text 用户文本
     * @return 结构化解析结果
     */
    AiParseBillResponse parseBill(Integer userId, Integer bookId, BillType type, String text);
}
