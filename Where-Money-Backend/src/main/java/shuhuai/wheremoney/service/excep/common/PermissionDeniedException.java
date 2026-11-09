package shuhuai.wheremoney.service.excep.common;

import shuhuai.wheremoney.service.excep.BaseException;

/**
 * 权限不足异常
 * 当用户没有操作权限时抛出此异常
 */
public class PermissionDeniedException extends BaseException {
    /**
     * 构造方法
     *
     * @param message 异常信息
     */
    public PermissionDeniedException(String message) {
        super(message);
    }
}