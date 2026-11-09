package shuhuai.wheremoney.service.excep.ai;

import shuhuai.wheremoney.service.excep.BaseException;

/**
 * AI解析结果歧义异常
 */
public class AiAmbiguousMatchException extends BaseException {
    public AiAmbiguousMatchException(String message) {
        super(message);
    }
}
