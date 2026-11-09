package shuhuai.wheremoney.service.excep.ai;

import shuhuai.wheremoney.service.excep.BaseException;

/**
 * AI解析限流异常
 */
public class AiRateLimitException extends BaseException {
    public AiRateLimitException(String message) {
        super(message);
    }
}
