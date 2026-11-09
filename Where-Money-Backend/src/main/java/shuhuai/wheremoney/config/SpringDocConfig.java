package shuhuai.wheremoney.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc配置类
 * 配置Swagger文档，包括API分组、安全方案和API信息
 */
@Configuration
public class SpringDocConfig {
    /**
     * 控制器扫描基础包路径
     */
    private static final String basePackage = "shuhuai.wheremoney.controller";

    /**
     * 配置API分组
     * @return GroupedOpenApi实例
     */
    @Bean
    public GroupedOpenApi usersGroup() {
        return GroupedOpenApi.builder()
                // 设置分组名称
                .group("users")
                // 添加操作自定义器，为每个操作添加安全要求
                .addOperationCustomizer((operation, handlerMethod) -> {
                    operation.addSecurityItem(new SecurityRequirement().addList("Authorization"));
                    return operation;
                })
                // 设置要扫描的包路径
                .packagesToScan(basePackage)
                .build();
    }

    /**
     * 配置OpenAPI
     * 设置安全方案和API信息
     * @return OpenAPI实例
     */
    @Bean
    public OpenAPI customOpenAPI() {
        Components components = new Components();
        // 添加安全方案，使用JWT认证
        components.addSecuritySchemes("Authorization",
                new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .name("Authorization")
                        .in(SecurityScheme.In.HEADER)
                        .description("请求头")
        );
        return new OpenAPI()
                // 设置组件
                .components(components)
                // 设置API信息
                .info(apiInfo());
    }

    /**
     * 配置API信息
     * 设置标题、版本、联系人、描述和许可证
     * @return Info实例
     */
    private Info apiInfo() {
        // 创建联系人信息
        Contact contact = new Contact();
        contact.setEmail("lvzhihe_123@qq.com");
        contact.setName("殊怀丶");
        contact.setUrl("http://lvshuhuai.cn");
        return new Info()
                // 设置API标题
                .title("钱去哪儿了")
                // 设置API版本
                .version("1.0")
                // 设置联系人信息
                .contact(contact)
                // 设置API描述
                .description("钱去哪儿了")
                // 设置许可证信息
                .license(new License().name("Apache 2.0").url("http://springdoc.org"));
    }
}