package shuhuai.wheremoney.service.excep.common;

import shuhuai.wheremoney.service.excep.BaseException;

/**
 * 服务器异常
 * 当服务器内部发生错误时抛出此异常
 *
 * @author 殊怀 丶
 * @version 1.0
 */
public class ServerException extends BaseException {
    /**
     * 构造方法
     *
     * @param message 异常信息
     */
    public ServerException(String message) {
        super(message);
    }
}