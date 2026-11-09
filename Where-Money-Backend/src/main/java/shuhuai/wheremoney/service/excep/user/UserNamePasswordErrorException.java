package shuhuai.wheremoney.service.excep.user;

import shuhuai.wheremoney.service.excep.BaseException;

/**
 * 用户名密码错误异常
 * 当用户登录时用户名或密码错误时抛出此异常
 */
public class UserNamePasswordErrorException extends BaseException {
    /**
     * 构造方法
     *
     * @param message 异常信息
     */
    public UserNamePasswordErrorException(String message) {
        super(message);
    }
}