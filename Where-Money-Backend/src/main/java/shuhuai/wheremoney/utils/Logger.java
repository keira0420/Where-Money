package shuhuai.wheremoney.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import shuhuai.wheremoney.response.Response;

import javax.annotation.Nonnull;

/**
 * 日志记录工具类
 * 实现ResponseBodyAdvice接口，用于在响应体写入之前记录请求日志
 * 对成功和失败的响应分别记录不同级别的日志
 */
@ControllerAdvice
@Slf4j
public class Logger implements ResponseBodyAdvice<Object> {
    /**
     * 判断是否支持对响应体进行处理
     *
     * @param methodParameter 方法参数
     * @param aClass          消息转换器类
     * @return 始终返回true，表示支持所有响应体处理
     */
    @Override
    public boolean supports(@Nonnull MethodParameter methodParameter, @Nonnull Class<? extends HttpMessageConverter<?>> aClass) {
        return true;
    }

    /**
     * 在响应体写入之前进行处理
     * 记录响应的日志信息，根据响应码判断是成功还是失败
     *
     * @param body              响应体
     * @param methodParameter   方法参数
     * @param mediaType         媒体类型
     * @param aClass            消息转换器类
     * @param serverHttpRequest 请求对象
     * @param serverHttpResponse 响应对象
     * @return 处理后的响应体
     */
    @Nullable
    @Override
    public Object beforeBodyWrite(@Nullable Object body, @Nonnull MethodParameter methodParameter, @Nonnull MediaType mediaType,
                                  @Nonnull Class<? extends HttpMessageConverter<?>> aClass, @Nonnull ServerHttpRequest serverHttpRequest,
                                  @Nonnull ServerHttpResponse serverHttpResponse) {
        if (body instanceof Response<?> response) {
            if (response.getCode() == 200) {
                log.info(RequestGetter.getRequestUrl() + "：" + response.getMessage());
            } else {
                log.error(RequestGetter.getRequestUrl() + "：" + response.getMessage());
            }
        }
        return body;
    }
}