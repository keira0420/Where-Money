package shuhuai.wheremoney.service;

import shuhuai.wheremoney.service.excep.common.ParamsException;
import shuhuai.wheremoney.service.excep.common.ServerException;
import shuhuai.wheremoney.service.excep.user.UserMissingException;
import shuhuai.wheremoney.service.excep.user.UserNameOccupiedException;
import shuhuai.wheremoney.service.excep.user.UserNamePasswordErrorException;

/**
 * 用户服务接口
 * 提供用户相关的业务逻辑操作，包括用户注册、登录、修改用户名和密码等功能
 */
public interface UserService {
    /**
     * 用户注册
     * @param userName 用户名
     * @param password 密码
     * @throws ServerException 服务器异常
     * @throws UserNameOccupiedException 用户名已被占用异常
     * @throws ParamsException 参数异常
     */
    void register(String userName, String password) throws ServerException, UserNameOccupiedException, ParamsException;

    /**
     * 用户登录
     * @param userName 用户名
     * @param password 密码
     * @throws UserNamePasswordErrorException 用户名或密码错误异常
     * @throws ParamsException 参数异常
     */
    void login(String userName, String password) throws UserNamePasswordErrorException, ParamsException;

    /**
     * 修改用户名
     * @param userId 用户ID
     * @param userName 新用户名
     * @throws UserNameOccupiedException 用户名已被占用异常
     * @throws UserMissingException 用户不存在异常
     * @throws ParamsException 参数异常
     * @throws ServerException 服务器异常
     */
    void changeUsername(Integer userId, String userName) throws UserNameOccupiedException, UserMissingException, ParamsException, ServerException;

    /**
     * 修改密码
     * @param userId 用户ID
     * @param password 新密码
     * @throws ParamsException 参数异常
     * @throws ServerException 服务器异常
     * @throws UserMissingException 用户不存在异常
     */
    void changePassword(Integer userId, String password) throws ParamsException, ServerException, UserMissingException;

    /**
     * 根据用户名获取用户ID
     * @param userName 用户名
     * @return 用户ID
     * @throws UserMissingException 用户不存在异常
     */
    Integer getUserId(String userName) throws UserMissingException;
}