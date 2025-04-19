package com.devin.s3;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 2025/4/16 14:54.
 *
 * <p></p>
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@SpringBootApplication
public class S3Application {

    /**
     * 主入口函数.
     * @param args 入口参数
     */
    public static void main(final String[] args) {
        SpringApplication.run(S3Application.class, args);
        log.info("======> S3 Application Started <======");
    }
}
