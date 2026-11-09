package shuhuai.wheremoney.service.excep.book;

import shuhuai.wheremoney.service.excep.BaseException;

/**
 * 标题重复异常
 * 当账本标题已存在时抛出此异常
 *
 * @author 殊怀丶
 * @version 1.0
 */
public class TitleOccupiedException extends BaseException {
    /**
     * 构造方法
     *
     * @param message 异常信息
     */
    public TitleOccupiedException(String message) {
        super(message);
    }
}