package shuhuai.wheremoney.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS配置类
 * 配置跨域资源共享，允许前端从不同的域名访问后端API
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @org.springframework.beans.factory.annotation.Value("${app.cors.origins:http://localhost:13140,http://127.0.0.1:13140}")
    private String[] origins;
    /**
     * 配置CORS映射
     * 设置允许的源、凭据、头部和方法
     * @param registry CORS注册表
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 允许所有源
                .allowedOrigins(origins)
                // 允许携带凭据
                .allowCredentials(true)
                // 允许所有头部
                .allowedHeaders(CorsConfiguration.ALL)
                // 允许所有方法
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .exposedHeaders("Authorization", "X-Request-Id");
    }
}
