package shuhuai.wheremoney.service.excep.ai;

import shuhuai.wheremoney.service.excep.BaseException;

/**
 * AI解析信息不全异常
 */
public class AiInfoMissingException extends BaseException {
    public AiInfoMissingException(String message) {
        super(message);
    }
}
