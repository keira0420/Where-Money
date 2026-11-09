package shuhuai.wheremoney.service.excep.ai;

import shuhuai.wheremoney.service.excep.BaseException;

/**
 * AI解析分类失败异常
 */
public class AiCategoryNotFoundException extends BaseException {
    public AiCategoryNotFoundException(String message) {
        super(message);
    }
}
