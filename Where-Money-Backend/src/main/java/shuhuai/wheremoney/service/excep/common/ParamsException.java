package shuhuai.wheremoney.service.excep.common;

import shuhuai.wheremoney.service.excep.BaseException;

/**
 * 参数异常
 * 当请求参数不符合要求时抛出此异常
 */
public class ParamsException extends BaseException {
    /**
     * 构造方法
     *
     * @param message 异常信息
     */
    public ParamsException(String message) {
        super(message);
    }
}