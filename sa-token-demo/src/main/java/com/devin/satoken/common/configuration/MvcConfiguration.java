package com.devin.satoken.common.configuration;

import com.devin.satoken.common.interceptor.SaTokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.List;

/**
 * 2025/4/19 17:19.
 *
 * <p></p>
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 */
@Configuration
public class MvcConfiguration implements WebMvcConfigurer {

    @Autowired
    private SaTokenInterceptor saTokenInterceptor;

    /**
     * 配置Swagger的排除路径.
     */
    private final List<String> swaggerExcludePathPatterns = List.of(
            "/doc.html",
            "/webjars/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/favicon.ico",
            "/error"
    );

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        // 开启Sa-Token的路由检验
        registry.addInterceptor(saTokenInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/sa-token/login")
                .excludePathPatterns(swaggerExcludePathPatterns);
    }
}
