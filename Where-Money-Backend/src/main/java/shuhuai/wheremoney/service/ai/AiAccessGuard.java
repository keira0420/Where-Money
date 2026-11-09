package shuhuai.wheremoney.service.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import shuhuai.wheremoney.entity.Book;
import shuhuai.wheremoney.mapper.BookMapper;
import shuhuai.wheremoney.service.excep.common.PermissionDeniedException;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class AiAccessGuard {
    private final BookMapper books;

    public Book requireBook(Integer userId, Integer bookId) {
        Book book = bookId == null ? null : books.selectBookById(bookId);
        if (userId == null || book == null || !Objects.equals(userId, book.getUserId())) {
            throw new PermissionDeniedException("无权访问该账本");
        }
        return book;
    }
}
