package shuhuai.wheremoney.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import shuhuai.wheremoney.service.excep.common.TokenExpireException;

import java.util.HashMap;
import java.util.Map;

/**
 * Token验证工具类
 * 实现HandlerInterceptor接口，用于拦截请求并验证Token
 * 提供Token的生成、解析和验证功能
 */
@Component
public class TokenValidator implements HandlerInterceptor {
    /**
     * 线程本地存储，用于存储用户信息
     */
    private final static ThreadLocal<Map<String, String>> threadLocal = new ThreadLocal<>();

    /**
     * Token私钥
     */
    @Value("${token.privateKey}")
    private String privateKey;

    /**
     * 年轻Token过期时间（毫秒）
     */
    @Value("${token.youngToken}")
    private Long youngToken;

    /**
     * 旧Token过期时间（毫秒）
     */
    @Value("${token.oldToken}")
    private Long oldToken;

    @jakarta.annotation.PostConstruct
    public void validateSecret() {
        if (privateKey == null || privateKey.length() < 32) throw new IllegalStateException("JWT_SECRET至少需要32个字符");
    }

    /**
     * 获取当前线程的用户信息
     *
     * @return 用户信息映射
     */
    public static Map<String, String> getUser() {
        return threadLocal.get();
    }

    /**
     * 设置当前线程的用户信息
     *
     * @param userIdentify 用户信息映射
     */
    public static void setUser(Map<String, String> userIdentify) {
        threadLocal.set(userIdentify);
    }

    /**
     * 移除当前线程的用户信息
     */
    public static void removeUser() {
        threadLocal.remove();
    }

    /**
     * 生成Token
     *
     * @param userId 用户ID
     * @return 生成的Token
     */
    public String getToken(Integer userId) {
        return JWT.create().withClaim("userId", userId.toString()).withClaim("timeStamp", System.currentTimeMillis()).sign(Algorithm.HMAC256(privateKey));
    }

    /**
     * 解析Token
     *
     * @param token Token字符串
     * @return 解析后的用户信息映射
     */
    public Map<String, String> parseToken(String token) {
        HashMap<String, String> map = new HashMap<>();
        DecodedJWT decodedjwt = JWT.require(Algorithm.HMAC256(privateKey)).build().verify(token);
        Claim userId = decodedjwt.getClaim("userId");
        Claim timeStamp = decodedjwt.getClaim("timeStamp");
        map.put("userId", userId.asString());
        map.put("timeStamp", timeStamp.asLong().toString());
        return map;
    }

    /**
     * 请求处理前的拦截方法
     * 验证Token的有效性，并处理Token的过期逻辑
     *
     * @param httpServletRequest  HttpServletRequest对象
     * @param httpServletResponse HttpServletResponse对象
     * @param object              处理请求的对象
     * @return 是否继续处理请求
     */
    @Override
    public boolean preHandle(@NonNull HttpServletRequest httpServletRequest, @NonNull HttpServletResponse httpServletResponse, @NonNull Object object) {
        if (!(object instanceof HandlerMethod)) {
            return true;
        }
        String token = httpServletRequest.getHeader("Authorization");
        if (null == token || token.trim().isEmpty()) {
            throw new TokenExpireException("token无效");
        }
        Map<String, String> map;
        try {
            token = token.split(" ")[1];
            map = parseToken(token);
        } catch (Exception e) {
            throw new TokenExpireException("token无效");
        }
        Integer userId = Integer.parseInt(map.get("userId"));
        long timeOfUse = System.currentTimeMillis() - Long.parseLong(map.get("timeStamp"));
        if (timeOfUse < -60000) throw new TokenExpireException("token无效");
        if (timeOfUse >= youngToken && timeOfUse < oldToken) {
            httpServletResponse.setHeader("Authorization", "Bearer " + getToken(userId));
        } else if (timeOfUse >= oldToken) {
            throw new TokenExpireException("token过期");
        }
        setUser(map);
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler, Exception ex) {
        removeUser();
    }
}
