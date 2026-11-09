package shuhuai.wheremoney.service.excep.user;

import shuhuai.wheremoney.service.excep.BaseException;

/**
 * 账号重复异常
 * 当用户名已被占用时抛出此异常
 *
 * @author 殊怀丶
 * @version 1.0
 */
public class UserNameOccupiedException extends BaseException {
    /**
     * 构造方法
     *
     * @param message 异常信息
     */
    public UserNameOccupiedException(String message) {
        super(message);
    }
}