package com.devin.s3.common.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 2025/4/16 17:17.
 *
 * <p>
 *     knife4j配置类
 * </p>
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 */
@Configuration
public class Knife4jConfiguration {
    /**
     * swagger配置.
     * @return OpenAPI
     */
    @Bean
    public OpenAPI customOpenAPI() {

        // 设置联系方式
        Contact contact = new Contact();
        contact.setName("devin");
        contact.setEmail("wzh.devin@gmail.com");
        contact.setUrl("https://github.com/wzh-devin");

        // 配置信息
        return new OpenAPI()
                .info(new Info()
                        .title("AWS S3服务")
                        .contact(contact)
                        .version("1.0")
                        .description("S3服务接口文档"));
    }
}
