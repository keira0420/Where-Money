package shuhuai.wheremoney.service.excep.ai;

import shuhuai.wheremoney.service.excep.BaseException;

/**
 * AI解析资产失败异常
 */
public class AiAssetNotFoundException extends BaseException {
    public AiAssetNotFoundException(String message) {
        super(message);
    }
}
