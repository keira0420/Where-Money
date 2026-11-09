package shuhuai.wheremoney.service.excep.user;

import shuhuai.wheremoney.service.excep.BaseException;

/**
 * 用户不存在异常
 * 当用户账号不存在时抛出此异常
 *
 * @author 殊怀丶
 * @version 1.0
 */
public class UserMissingException extends BaseException {
    /**
     * 构造方法
     *
     * @param message 异常信息
     */
    public UserMissingException(String message) {
        super(message);
    }
}