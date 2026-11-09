package shuhuai.wheremoney.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 用户实体类
 * 用于表示系统用户信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
    private Integer id;
    private String userName;
    private String hashedPassword;
    private Boolean isActive;
    private Timestamp createTime;

    /**
     * 构造方法
     *
     * @param userName       用户名
     * @param hashedPassword 哈希后的密码
     */
    public User(String userName, String hashedPassword) {
        this.userName = userName;
        this.hashedPassword = hashedPassword;
        this.isActive = true;
    }

    /**
     * 获取用户ID
     *
     * @return 用户ID
     */
    public Integer getId() {
        return id;
    }

    /**
     * 设置用户ID
     *
     * @param id 用户ID
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 获取用户名
     *
     * @return 用户名
     */
    public String getUserName() {
        return userName;
    }

    /**
     * 设置用户名
     *
     * @param userName 用户名
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * 获取哈希后的密码
     *
     * @return 哈希后的密码
     */
    public String getHashedPassword() {
        return hashedPassword;
    }

    /**
     * 设置哈希后的密码
     *
     * @param hashedPassword 哈希后的密码
     */
    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    /**
     * 获取用户是否激活
     *
     * @return 用户是否激活
     */
    public Boolean getIsActive() {
        return isActive;
    }

    /**
     * 设置用户是否激活
     *
     * @param isActive 用户是否激活
     */
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    /**
     * 获取用户创建时间
     *
     * @return 用户创建时间
     */
    public Timestamp getCreateTime() {
        return createTime;
    }

    /**
     * 设置用户创建时间
     *
     * @param createTime 用户创建时间
     */
    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }
}