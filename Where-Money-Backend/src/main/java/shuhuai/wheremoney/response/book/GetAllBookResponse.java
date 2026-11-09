package shuhuai.wheremoney.response.book;

import shuhuai.wheremoney.entity.Book;

import java.util.List;

/**
 * 获取所有账本响应类
 * 用于返回用户的所有账本信息
 */
public class GetAllBookResponse {
    private List<Book> bookList;

    /**
     * 构造方法
     *
     * @param bookList 账本列表
     */
    public GetAllBookResponse(List<Book> bookList) {
        this.bookList = bookList;
    }

    /**
     * 获取账本列表
     *
     * @return 账本列表
     */
    public List<Book> getBookList() {
        return bookList;
    }

    /**
     * 设置账本列表
     *
     * @param bookList 账本列表
     */
    public void setBookList(List<Book> bookList) {
        this.bookList = bookList;
    }
}