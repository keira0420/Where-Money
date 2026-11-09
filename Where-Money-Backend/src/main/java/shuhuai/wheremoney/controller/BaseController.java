package shuhuai.wheremoney.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.exceptions.PersistenceException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.mybatis.spring.MyBatisSystemException;
import shuhuai.wheremoney.response.Response;
import shuhuai.wheremoney.service.excep.BaseException;
import shuhuai.wheremoney.service.excep.ai.AiAmbiguousMatchException;
import shuhuai.wheremoney.service.excep.ai.AiAssetNotFoundException;
import shuhuai.wheremoney.service.excep.ai.AiCategoryNotFoundException;
import shuhuai.wheremoney.service.excep.ai.AiInfoMissingException;
import shuhuai.wheremoney.service.excep.ai.AiInvokeException;
import shuhuai.wheremoney.service.excep.ai.AiIrrelevantTextException;
import shuhuai.wheremoney.service.excep.ai.AiRateLimitException;
import shuhuai.wheremoney.service.excep.ai.AiResponseFormatException;
import shuhuai.wheremoney.service.excep.ai.AiTimeoutException;
import shuhuai.wheremoney.service.excep.ai.AiUnsupportedTypeException;
import shuhuai.wheremoney.service.excep.book.TitleOccupiedException;
import shuhuai.wheremoney.service.excep.common.ParamsException;
import shuhuai.wheremoney.service.excep.common.PermissionDeniedException;
import shuhuai.wheremoney.service.excep.common.ServerException;
import shuhuai.wheremoney.service.excep.common.TokenExpireException;
import shuhuai.wheremoney.service.excep.user.UserMissingException;
import shuhuai.wheremoney.service.excep.user.UserNameOccupiedException;
import shuhuai.wheremoney.service.excep.user.UserNamePasswordErrorException;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 基础控制器类
 * 所有控制器的父类，用于统一处理异常
 */
@Slf4j
@org.springframework.web.bind.annotation.RestControllerAdvice
public class BaseController {
    /**
     * 处理服务层异常
     * @param error 服务层异常对象
     * @return 异常响应结果
     */
    @ExceptionHandler(BaseException.class)
    public Response<Object> handleServiceException(BaseException error) {
        // 创建响应对象
        Response<Object> response = new Response<>();
        // 根据异常类型设置响应码
        if (error instanceof UserNameOccupiedException || error instanceof TitleOccupiedException) {
            // 用户名已被占用或标题已被占用
            response.setCode(400);
        } else if (error instanceof UserNamePasswordErrorException || error instanceof TokenExpireException) {
            // 用户名密码错误或token过期
            response.setCode(401);
        } else if (error instanceof ParamsException) {
            // 参数错误
            response.setCode(422);
        } else if (error instanceof ServerException) {
            // 服务器错误
            response.setCode(500);
        } else if (error instanceof UserMissingException) {
            // 用户不存在
            response.setCode(404);
        } else if (error instanceof PermissionDeniedException) {
            // 权限不足
            response.setCode(403);
        } else if (error instanceof AiRateLimitException) {
            // AI解析限流
            response.setCode(429);
        } else if (error instanceof AiUnsupportedTypeException
                || error instanceof AiIrrelevantTextException
                || error instanceof AiInfoMissingException
                || error instanceof AiAssetNotFoundException
                || error instanceof AiCategoryNotFoundException
                || error instanceof AiAmbiguousMatchException) {
            // AI解析业务失败
            response.setCode(422);
        } else if (error instanceof AiInvokeException
                || error instanceof AiTimeoutException
                || error instanceof AiResponseFormatException) {
            // AI调用或模型输出异常
            response.setCode(500);
        }
        // 设置响应消息
        response.setMessage(error.getMessage());
        return response;
    }

    /**
     * 处理Spring参数异常
     * @param error Spring参数异常对象
     * @return 异常响应结果
     */
    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            BindException.class,
            MethodArgumentNotValidException.class,
            HttpMessageNotReadableException.class,
            MissingServletRequestPartException.class
    })
    public Response<Object> handleSpringParamsException(Exception error) {
        // 返回参数错误响应
        return new Response<>(422, "参数错误", null);
    }

    /**
     * 处理数据库异常
     * @param error 数据库异常对象
     * @return 异常响应结果
     */
    @ExceptionHandler({
            MyBatisSystemException.class,
            PersistenceException.class,
            BindingException.class,
            DataAccessException.class,
            SQLException.class,
            DuplicateKeyException.class
    })
    public Response<Object> handleDatabaseException(Exception error) {
        if (error instanceof org.springframework.dao.ConcurrencyFailureException) return new Response<>(409, "数据正在被其他请求修改，请重试", null);
        // 记录异常日志
        log.error("数据库/MyBatis异常", error);
        // 处理数据完整性异常
        if (error instanceof DataIntegrityViolationException
                || error instanceof SQLIntegrityConstraintViolationException) {
            return new Response<>(422, "参数错误", null);
        }
        // 返回数据库错误响应
        return new Response<>(500, "数据库错误", null);
    }

    /**
     * 处理未捕获的异常
     * @param error 未捕获的异常对象
     * @return 异常响应结果
     */
    @ExceptionHandler(Exception.class)
    public Response<Object> handleException(Exception error) {
        // 记录异常日志
        log.error("未处理异常", error);
        // 返回服务器错误响应
        return new Response<>(500, "服务器错误", null);
    }
}
