package shuhuai.wheremoney.service.excep;

/**
 * 业务层异常类基类
 * 所有业务异常都应继承此类
 *
 * @author 殊怀丶
 * @version 1.0
 */
public class BaseException extends RuntimeException {
    /**
     * 构造方法
     *
     * @param message 异常信息
     */
    public BaseException(String message) {
        super(message);
    }
}