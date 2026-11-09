package shuhuai.wheremoney.service.excep.ai;

import shuhuai.wheremoney.service.excep.BaseException;

/**
 * AI调用超时异常
 */
public class AiTimeoutException extends BaseException {
    public AiTimeoutException(String message) {
        super(message);
    }
}
