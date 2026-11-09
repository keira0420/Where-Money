package shuhuai.wheremoney.service.excep.ai;

import shuhuai.wheremoney.service.excep.BaseException;

/**
 * AI返回格式异常
 */
public class AiResponseFormatException extends BaseException {
    public AiResponseFormatException(String message) {
        super(message);
    }
}
