package shuhuai.wheremoney.service.excep.ai;

import shuhuai.wheremoney.service.excep.BaseException;

/**
 * AI不支持的账单类型异常
 */
public class AiUnsupportedTypeException extends BaseException {
    public AiUnsupportedTypeException(String message) {
        super(message);
    }
}
