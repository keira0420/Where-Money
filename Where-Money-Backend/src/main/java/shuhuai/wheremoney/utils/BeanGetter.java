package shuhuai.wheremoney.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

/**
 * Bean获取工具类
 * 实现ApplicationContextAware接口，用于获取Spring容器中的Bean实例
 * 通过静态方法提供全局访问Spring容器的能力
 */
@Component
public class BeanGetter implements ApplicationContextAware {

    /**
     * Spring应用上下文
     */
    public static ApplicationContext context;

    /**
     * 设置应用上下文
     * 当Spring容器初始化时，会自动调用此方法注入ApplicationContext
     *
     * @param context 应用上下文
     * @throws BeansException 设置上下文时可能抛出的异常
     */
    @Override
    public void setApplicationContext(@Nonnull ApplicationContext context) throws BeansException {
        BeanGetter.context = context;
    }

    /**
     * 根据类型获取Bean实例
     *
     * @param <T>       Bean类型
     * @param beanClass Bean的Class对象
     * @return 指定类型的Bean实例
     */
    public static <T> T getBean(Class<T> beanClass) {
        return context.getBean(beanClass);
    }

    /**
     * 获取应用上下文
     *
     * @return 应用上下文
     */
    public static ApplicationContext getContext() {
        return context;
    }
}