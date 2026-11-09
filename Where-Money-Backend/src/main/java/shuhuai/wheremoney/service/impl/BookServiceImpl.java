package shuhuai.wheremoney.service.impl;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shuhuai.wheremoney.entity.BillCategory;
import shuhuai.wheremoney.entity.Book;
import shuhuai.wheremoney.entity.User;
import shuhuai.wheremoney.mapper.*;
import shuhuai.wheremoney.service.BillCategoryService;
import shuhuai.wheremoney.service.BookService;
import shuhuai.wheremoney.service.BudgetService;
import shuhuai.wheremoney.service.excep.book.TitleOccupiedException;
import shuhuai.wheremoney.service.excep.common.ParamsException;
import shuhuai.wheremoney.service.excep.common.PermissionDeniedException;
import shuhuai.wheremoney.service.excep.common.ServerException;
import shuhuai.wheremoney.service.excep.user.UserMissingException;
import shuhuai.wheremoney.type.BillType;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

/**
 * 账本服务实现类
 * 实现BookService接口，提供账本相关的业务逻辑操作
 */
@Service
public class BookServiceImpl implements BookService {
    /**
     * 默认SVG图标
     */
    @Value("${default.svg}")
    private String defaultSvg;
    /**
     * 账本Mapper
     */
    @Resource
    private BookMapper bookMapper;
    /**
     * 用户Mapper
     */
    @Resource
    private UserMapper userMapper;
    /**
     * 账单分类服务
     */
    @Resource
    private BillCategoryService billCategoryService;
    /**
     * 预算服务
     */
    @Resource
    private BudgetService budgetService;
    /**
     * 支出账单Mapper
     */
    @Resource
    private PayBillMapper payBillMapper;
    /**
     * 退款账单Mapper
     */
    @Resource
    private RefundBillMapper refundBillMapper;
    /**
     * 收入账单Mapper
     */
    @Resource
    private IncomeBillMapper incomeBillMapper;
    /**
     * 转账账单Mapper
     */
    @Resource
    private TransferBillMapper transferBillMapper;
    /**
     * 账单分类Mapper
     */
    @Resource
    private BillCategoryMapper billCategoryMapper;
    /**
     * 预算Mapper
     */
    @Resource
    private BudgetMapper budgetMapper;

    /**
     * 添加账本
     * @param userId 用户ID
     * @param title 账本标题
     * @param beginDate 开始日期
     * @throws ParamsException 参数错误异常
     * @throws UserMissingException 用户不存在异常
     * @throws TitleOccupiedException 标题已被占用异常
     * @throws ServerException 服务器错误异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addBook(Integer userId, String title, Integer beginDate) {
        // 参数校验
        if (userId == null || title == null || beginDate == null || beginDate < 1 || beginDate > 28) {
            throw new ParamsException("参数错误");
        }
        // 查询用户是否存在
        User user = userMapper.selectUserByUserId(userId);
        // 查询账本标题是否已被占用
        Book book = bookMapper.selectBookByUserTitle(user, title);
        if (book != null) {
            throw new TitleOccupiedException("标题已被占用");
        }
        // 创建新账本
        book = new Book(user.getId(), title, beginDate);
        // 插入账本
        Integer result = bookMapper.insertBookSelective(book);
        if (result != 1) {
            throw new ServerException("服务器错误");
        }
        // 添加默认账单分类
        billCategoryService.addDefaultBillCategory(book.getId());
    }

    /**
     * 获取用户的账本列表
     * @param userId 用户ID
     * @return 账本列表
     * @throws ParamsException 参数错误异常
     * @throws UserMissingException 用户不存在异常
     */
    @Override
    public List<Book> getBookList(Integer userId) {
        // 参数校验
        if (userId == null) {
            throw new ParamsException("参数错误");
        }
        // 查询用户是否存在
        User user = userMapper.selectUserByUserId(userId);
        if (user == null) {
            throw new UserMissingException("用户不存在");
        }
        // 查询用户的所有账本
        List<Book> books = bookMapper.selectBookByUser(user);
        // 重建每个账本的预算
        for (Book book : books) {
            if (book != null && book.getId() != null) {
                budgetService.rebuildBudgetByBook(book.getId());
            }
        }
        // 重新查询用户的所有账本
        return bookMapper.selectBookByUser(user);
    }

    /**
     * 获取指定账本
     * @param id 账本ID
     * @return 账本实体
     * @throws ParamsException 参数错误异常
     */
    @Override
    public Book getBook(Integer id) {
        // 参数校验
        if (id == null) {
            throw new ParamsException("参数错误");
        }
        // 查询账本是否存在
        Book book = bookMapper.selectBookById(id);
        if (book == null) {
            return null;
        }
        // 重建账本的预算
        budgetService.rebuildBudgetByBook(id);
        // 重新查询账本
        return bookMapper.selectBookById(id);
    }

    /**
     * 获取账本月支出
     * @param bookId 账本ID
     * @param month 月份
     * @return 支出金额
     * @throws ParamsException 参数错误异常
     */
    @Override
    public BigDecimal getPayMonth(Integer bookId, Timestamp month) {
        // 参数校验
        if (bookId == null || month == null) {
            throw new ParamsException("参数错误");
        }
        // 查询账本月支出
        return bookMapper.selectPayMonthByBookId(bookId, month);
    }

    /**
     * 获取账本月收入
     * @param bookId 账本ID
     * @param month 月份
     * @return 收入金额
     * @throws ParamsException 参数错误异常
     */
    @Override
    public BigDecimal getIncomeMonth(Integer bookId, Timestamp month) {
        // 参数校验
        if (bookId == null) {
            throw new ParamsException("参数错误");
        }
        // 查询账本月收入
        return bookMapper.selectIncomeMonthByBookId(bookId, month);
    }

    /**
     * 获取账本月结余
     * @param bookId 账本ID
     * @param month 月份
     * @return 结余金额
     * @throws ParamsException 参数错误异常
     */
    @Override
    public BigDecimal getBalanceMonth(Integer bookId, Timestamp month) {
        // 参数校验
        if (bookId == null) {
            throw new ParamsException("参数错误");
        }
        // 查询账本月结余
        return bookMapper.selectBalanceMonthByBookId(bookId, month);
    }

    /**
     * 获取账本月退款
     * @param bookId 账本ID
     * @param month 月份
     * @return 退款金额
     * @throws ParamsException 参数错误异常
     */
    @Override
    public BigDecimal getRefundMonth(Integer bookId, Timestamp month) {
        // 参数校验
        if (bookId == null) {
            throw new ParamsException("参数错误");
        }
        // 查询账本月退款
        return bookMapper.selectRefundMonthByBookId(bookId, month);
    }

    /**
     * 获取所有账单分类
     * @param bookId 账本ID
     * @param type 账单类型
     * @return 账单分类列表
     * @throws ParamsException 参数错误异常
     */
    @Override
    public List<BillCategory> getAllBillCategory(Integer bookId, BillType type) {
        // 参数校验
        if (bookId == null) {
            throw new ParamsException("参数错误");
        }
        // 查询账本指定类型的账单分类
        return billCategoryService.getBillCategoriesByBookType(bookId, type);
    }

    /**
     * 添加账单分类
     * @param bookId 账本ID
     * @param billCategoryName 分类名称
     * @param svg 分类图标
     * @param type 分类类型
     * @throws ParamsException 参数错误异常
     */
    @Override
    public void addBillCategory(Integer bookId, String billCategoryName, String svg, BillType type) {
        // 参数校验
        if (bookId == null || billCategoryName == null || type == null) {
            throw new ParamsException("参数错误");
        }
        // 如果图标为空，使用默认图标
        if (svg == null) {
            svg = defaultSvg;
        }
        // 添加账单分类
        billCategoryService.addBillCategory(bookId, billCategoryName, svg, type);
    }

    /**
     * 删除账单分类
     * @param id 分类ID
     * @throws ParamsException 参数错误异常
     */
    @Override
    public void deleteBillCategory(Integer id) {
        // 参数校验
        if (id == null) {
            throw new ParamsException("参数错误");
        }
        // 删除账单分类
        billCategoryService.deleteBillCategory(id);
    }

    /**
     * 更新账单分类
     * @param id 分类ID
     * @param billCategoryName 分类名称
     * @param svg 分类图标
     * @param type 分类类型
     * @param bookId 账本ID
     * @throws ParamsException 参数错误异常
     */
    @Override
    public void updateBillCategory(Integer id, String billCategoryName, String svg, BillType type, Integer bookId) {
        // 参数校验
        if (id == null) {
            throw new ParamsException("参数错误");
        }
        // 更新账单分类
        billCategoryService.updateBillCategory(id, billCategoryName, svg, type, bookId);
    }

    /**
     * 删除账本
     * @param id 账本ID
     * @param userId 用户ID
     * @throws ParamsException 参数错误异常
     * @throws UserMissingException 用户不存在异常
     * @throws PermissionDeniedException 权限不足异常
     * @throws ServerException 服务器错误异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBook(Integer id, Integer userId) {
        // 参数校验
        if (id == null || userId == null) {
            throw new ParamsException("参数错误");
        }
        // 查询用户是否存在
        User user = userMapper.selectUserByUserId(userId);
        if (user == null) {
            throw new UserMissingException("用户不存在");
        }
        // 查询账本是否存在
        Book book = bookMapper.selectBookById(id);
        if (book == null) {
            throw new ParamsException("账本不存在");
        }
        // 检查用户是否有删除账本的权限
        if (!userId.equals(book.getUserId())) {
            throw new PermissionDeniedException("权限不足");
        }
        // 查询用户的所有账本
        List<Book> books = bookMapper.selectBookByUser(user);
        // 检查是否至少保留一个账本
        if (books.size() == 1) {
            throw new ParamsException("至少保留一个账本");
        }
        // 删除账本相关的所有数据
        refundBillMapper.deleteRefundBillByBookId(id);
        transferBillMapper.deleteTransferBillByBookId(id);
        incomeBillMapper.deleteIncomeBillByBookId(id);
        payBillMapper.deletePayBillByBookId(id);

        budgetMapper.deleteBudgetByBookId(id);
        billCategoryMapper.deleteBillCategoryByBookId(id);
        // 删除账本
        Integer result = bookMapper.deleteBook(id);
        if (result != 1) {
            throw new ServerException("服务器错误");
        }
    }
}