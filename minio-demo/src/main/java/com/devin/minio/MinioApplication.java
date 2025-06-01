package com.devin.minio;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 2025/6/1 14:28.
 *
 * <p>
 * MinioApplication
 * </p>
 *
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@SpringBootApplication
public class MinioApplication {

    /**
     * 主入口函数.
     *
     * @param args 入口参数
     */
    public static void main(final String[] args) {
        SpringApplication.run(MinioApplication.class, args);
        log.info("======> Sa-Token Application Started <======");
    }
}
