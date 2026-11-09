package shuhuai.wheremoney.response;

import java.io.Serializable;

/**
 * 响应实体类
 * 用于封装API响应数据
 *
 * @param <Type> 响应数据类型
 */
public class Response<Type> implements Serializable {
    private Integer code;
    private String message;
    private Type data;

    /**
     * 构造方法
     * 初始化默认值
     */
    public Response() {
        code = 0;
        message = "";
        data = null;
    }

    /**
     * 构造方法
     *
     * @param code 响应码
     */
    public Response(Integer code) {
        this.code = code;
    }

    /**
     * 构造方法
     *
     * @param error 异常对象
     */
    public Response(Throwable error) {
        this.message = error.getMessage();
    }

    /**
     * 构造方法
     *
     * @param code 响应码
     * @param data 响应数据
     */
    public Response(Integer code, Type data) {
        this.code = code;
        this.data = data;
    }

    /**
     * 构造方法
     *
     * @param code    响应码
     * @param message 响应消息
     * @param data    响应数据
     */
    public Response(Integer code, String message, Type data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 获取响应码
     *
     * @return 响应码
     */
    public Integer getCode() {
        return code;
    }

    /**
     * 设置响应码
     *
     * @param code 响应码
     */
    public void setCode(Integer code) {
        this.code = code;
    }

    /**
     * 获取响应消息
     *
     * @return 响应消息
     */
    public String getMessage() {
        return message;
    }

    /**
     * 设置响应消息
     *
     * @param message 响应消息
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 获取响应数据
     *
     * @return 响应数据
     */
    public Type getData() {
        return data;
    }

    /**
     * 设置响应数据
     *
     * @param data 响应数据
     */
    public void setData(Type data) {
        this.data = data;
    }
}