package shuhuai.wheremoney.response.user;

/**
 * 登录响应类
 * 用于返回登录成功后的用户token
 */
public class LoginResponse {
    private String token;

    /**
     * 构造方法
     *
     * @param token 用户登录成功后的token
     */
    public LoginResponse(String token) {
        this.token = token;
    }

    /**
     * 获取用户token
     *
     * @return 用户token
     */
    public String getToken() {
        return token;
    }

    /**
     * 设置用户token
     *
     * @param token 用户token
     */
    public void setToken(String token) {
        this.token = token;
    }
}