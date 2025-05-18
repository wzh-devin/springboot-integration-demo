package com.devin.xxljob.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 2025/5/16 15:05.
 *
 * <p></p>
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 */
@Data
@ConfigurationProperties(prefix = "xxl-job")
@Configuration
public class XxlJobProperties {

    private String accessToken;
    private Admin admin;
    private Executor executor;

    @Data
    public static class Admin {
        private String address;
    }

    @Data
    public static class Executor {
        private String appName;
        private String ip;
        private Integer port;
        private String logPath;
        private Integer logRetentionDays;
    }
}
