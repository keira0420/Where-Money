package shuhuai.wheremoney.response.user;

/**
 * 修改用户名响应类
 * 用于返回修改用户名后的新token
 */
public class ChangeUserNameResponse {
    private String token;

    /**
     * 构造方法
     *
     * @param token 新的用户token
     */
    public ChangeUserNameResponse(String token) {
        this.token = token;
    }

    /**
     * 获取新的用户token
     *
     * @return 新的用户token
     */
    public String getToken() {
        return token;
    }

    /**
     * 设置新的用户token
     *
     * @param token 新的用户token
     */
    public void setToken(String token) {
        this.token = token;
    }
}