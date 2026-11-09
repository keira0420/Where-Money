package shuhuai.wheremoney.service.excep.ai;

import shuhuai.wheremoney.service.excep.BaseException;

/**
 * AI调用异常
 */
public class AiInvokeException extends BaseException {
    public AiInvokeException(String message) {
        super(message);
    }
}
