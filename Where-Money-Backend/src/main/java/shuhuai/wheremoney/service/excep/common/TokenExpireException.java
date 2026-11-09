package shuhuai.wheremoney.service.excep.common;

import shuhuai.wheremoney.service.excep.BaseException;

/**
 * Token过期异常
 * 当用户Token无效或过期时抛出此异常
 */
public class TokenExpireException extends BaseException {
    /**
     * 构造方法
     *
     * @param message 异常信息
     */
    public TokenExpireException(String message) {
        super(message);
    }
}