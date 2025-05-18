package com.devin.xxljob;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 2025/5/16 14:43.
 *
 * <p></p>
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 */
@SpringBootApplication
@Slf4j
public class XxlJobApplication {
    /**
     * 主入口函数.
     * @param args 入口参数
     */
    public static void main(final String[] args) {
        SpringApplication.run(XxlJobApplication.class, args);
        log.info("======> XXL-Job Application Started <======");
    }
}
