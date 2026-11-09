package shuhuai.wheremoney.service.impl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shuhuai.wheremoney.entity.Book;
import shuhuai.wheremoney.entity.User;
import shuhuai.wheremoney.mapper.BookMapper;
import shuhuai.wheremoney.mapper.UserMapper;
import shuhuai.wheremoney.service.BillCategoryService;
import shuhuai.wheremoney.service.UserService;
import shuhuai.wheremoney.service.excep.common.ParamsException;
import shuhuai.wheremoney.service.excep.common.ServerException;
import shuhuai.wheremoney.service.excep.user.UserMissingException;
import shuhuai.wheremoney.service.excep.user.UserNameOccupiedException;
import shuhuai.wheremoney.service.excep.user.UserNamePasswordErrorException;
import shuhuai.wheremoney.utils.HashComputer;
import shuhuai.wheremoney.utils.PasswordHasher;

/**
 * 用户服务实现类
 * 实现UserService接口，提供用户相关的业务逻辑操作，包括注册、登录、修改用户名和密码等
 */
@Service
public class UserServiceImpl implements UserService {
    /**
     * 用户Mapper
     */
    @jakarta.annotation.Resource
    private UserMapper userMapper;
    /**
     * 账本Mapper
     */
    @Resource
    private BookMapper bookMapper;
    /**
     * 账单分类服务
     */
    @jakarta.annotation.Resource
    private BillCategoryService billCategoryService;

    /**
     * 用户注册
     * @param userName 用户名
     * @param password 密码
     * @throws ServerException 服务器错误异常
     * @throws UserNameOccupiedException 用户名已被占用异常
     * @throws ParamsException 参数错误异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void register(String userName, String password) throws ServerException, UserNameOccupiedException, ParamsException {
        // 参数校验
        if (userName == null || password == null) {
            throw new ParamsException("参数错误");
        }
        if (userName.isBlank() || userName.length() > 64) throw new ParamsException("账号长度应为1至64位");
        PasswordHasher.validate(password);
        // 检查用户名是否已被占用
        User sameName = userMapper.selectUserByUserName(userName);
        if (sameName != null) {
            throw new UserNameOccupiedException("用户名已被占用");
        }
        // 对密码进行哈希处理
        String hashedPassword = PasswordHasher.encode(password);
        // 创建用户实体
        User user = new User(userName, hashedPassword);
        // 插入用户
        Integer result = userMapper.insertUserSelective(user);
        if (result != 1) {
            throw new ServerException("服务器错误");
        }
        // 创建默认账本
        Book book = new Book(user.getId(), "默认账本", 1);
        result = bookMapper.insertBookSelective(book);
        if (result != 1) {
            throw new ServerException("服务器错误");
        }
        // 为默认账本添加默认账单分类
        billCategoryService.addDefaultBillCategory(book.getId());
    }

    /**
     * 用户登录
     * @param userName 用户名
     * @param password 密码
     * @throws UserNamePasswordErrorException 用户名或密码错误异常
     * @throws ParamsException 参数错误异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void login(String userName, String password) throws UserNamePasswordErrorException, ParamsException {
        // 参数校验
        if (userName == null || password == null) {
            throw new ParamsException("参数错误");
        }
        // 查询用户是否存在
        User result = userMapper.selectUserByUserName(userName);
        // 对密码进行哈希处理
        // 检查用户名和密码是否正确
        if (result == null || !PasswordHasher.matches(password, result.getHashedPassword())) {
            throw new UserNamePasswordErrorException("账户或密码错误");
        }
        if (!result.getHashedPassword().startsWith("$2")) {
            result.setHashedPassword(PasswordHasher.upgradeLegacy(password));
            userMapper.updateUserSelectiveById(result);
        }
    }

    /**
     * 修改用户名
     * @param userId 用户ID
     * @param userName 新用户名
     * @throws UserNameOccupiedException 用户名已被占用异常
     * @throws UserMissingException 用户不存在异常
     * @throws ParamsException 参数错误异常
     * @throws ServerException 服务器错误异常
     */
    @Override
    public void changeUsername(Integer userId, String userName) throws UserNameOccupiedException, UserMissingException, ParamsException, ServerException {
        // 参数校验
        if (userId == null || userName == null) {
            throw new ParamsException("参数错误");
        }
        // 查询用户是否存在
        User user = userMapper.selectUserByUserId(userId);
        if (user == null) {
            throw new UserMissingException("用户不存在");
        }
        // 检查新用户名是否已被占用
        if (userMapper.selectUserByUserName(userName) != null) {
            throw new UserNameOccupiedException("用户名已被占用");
        }
        // 更新用户名
        user.setUserName(userName);
        Integer result = userMapper.updateUserSelectiveById(user);
        if (result != 1) {
            throw new ServerException("服务器错误");
        }
    }

    /**
     * 修改密码
     * @param userId 用户ID
     * @param password 新密码
     * @throws ParamsException 参数错误异常
     * @throws ServerException 服务器错误异常
     * @throws UserMissingException 用户不存在异常
     */
    @Override
    public void changePassword(Integer userId, String password) throws ParamsException, ServerException, UserMissingException {
        // 参数校验
        if (userId == null || password == null) {
            throw new ParamsException("参数错误");
        }
        // 查询用户是否存在
        User user = userMapper.selectUserByUserId(userId);
        if (user == null) {
            throw new UserMissingException("用户不存在");
        }
        // 对新密码进行哈希处理
        user.setHashedPassword(PasswordHasher.encode(password));
        // 更新密码
        Integer result = userMapper.updateUserSelectiveById(user);
        if (result != 1) {
            throw new ServerException("服务器错误");
        }
    }

    /**
     * 根据用户名获取用户ID
     * @param userName 用户名
     * @return 用户ID
     * @throws UserMissingException 用户不存在异常
     */
    @Override
    public Integer getUserId(String userName) throws UserMissingException {
        // 查询用户是否存在
        User user = userMapper.selectUserByUserName(userName);
        if (user == null) {
            throw new UserMissingException("用户不存在");
        }
        return user.getId();
    }
}
