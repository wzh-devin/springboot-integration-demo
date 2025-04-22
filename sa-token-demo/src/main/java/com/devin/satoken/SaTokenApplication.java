package com.devin.satoken;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 2025/4/19 16:18.
 *
 * <p>
 *     程序入口函数
 * </p>
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@SpringBootApplication
public class SaTokenApplication {

    /**
     * 主入口函数.
     * @param args 入口参数
     */
    public static void main(final String[] args) {
        SpringApplication.run(SaTokenApplication.class, args);
        log.info("======> Sa-Token Application Started <======");
    }
}
