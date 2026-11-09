package shuhuai.wheremoney.mapper;

import org.apache.ibatis.annotations.Mapper;
import shuhuai.wheremoney.entity.User;

/**
 * 用户Mapper接口
 * 用于操作数据库中的user表，提供用户的CRUD操作
 */
@Mapper
public interface UserMapper {
    /**
     * 插入用户
     * @param user 用户数据
     * @return 受影响行数，判断是否成功
     */
    Integer insertUser(User user);

    /**
     * 选择性插入用户
     * @param user 用户数据
     * @return 受影响行数，判断是否成功
     */
    Integer insertUserSelective(User user);

    /**
     * 根据id删除用户
     * @param id 用户id
     * @return 受影响行数
     */
    Integer deleteUserByUserId(Integer id);

    /**
     * 更新用户
     * @param user 用户数据
     * @return 受影响行数
     */
    Integer updateUserById(User user);

    /**
     * 选择性更新用户
     * @param user 用户数据
     * @return 受影响行数
     */
    Integer updateUserSelectiveById(User user);

    /**
     * 根据用户名查询用户数据
     * @param userName 用户名
     * @return 对应用户数据，未找到则返回 null
     */
    User selectUserByUserName(String userName);

    /**
     * 根据用户id查询用户数据
     * @param id 用户id
     * @return 对应用户数据，未找到则返回 null
     */
    User selectUserByUserId(Integer id);
}