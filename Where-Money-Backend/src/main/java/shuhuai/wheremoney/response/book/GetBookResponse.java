package shuhuai.wheremoney.response.book;

import shuhuai.wheremoney.entity.Book;

/**
 * 获取单个账本响应类
 * 用于返回单个账本的详细信息
 */
public class GetBookResponse {
    private Book book;

    /**
     * 构造方法
     *
     * @param book 账本对象
     */
    public GetBookResponse(Book book) {
        this.book = book;
    }

    /**
     * 获取账本对象
     *
     * @return 账本对象
     */
    public Book getBook() {
        return book;
    }

    /**
     * 设置账本对象
     *
     * @param book 账本对象
     */
    public void setBook(Book book) {
        this.book = book;
    }
}