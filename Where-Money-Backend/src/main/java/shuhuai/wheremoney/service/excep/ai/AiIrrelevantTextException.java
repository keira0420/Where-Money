package shuhuai.wheremoney.service.excep.ai;

import shuhuai.wheremoney.service.excep.BaseException;

/**
 * AI识别为无关文本异常
 */
public class AiIrrelevantTextException extends BaseException {
    public AiIrrelevantTextException(String message) {
        super(message);
    }
}
