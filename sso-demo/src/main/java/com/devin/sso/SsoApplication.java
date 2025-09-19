package com.devin.sso;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 2025/9/17 21:18.
 *
 * <p></p>
 *
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@SpringBootApplication
public class SsoApplication {

    /**
     * 主入口函数.
     * @param args 入口参数
     */
    public static void main(final String[] args) {
        SpringApplication.run(SsoApplication.class, args);
        log.info("======> SSO Application Started <======");
    }
}
