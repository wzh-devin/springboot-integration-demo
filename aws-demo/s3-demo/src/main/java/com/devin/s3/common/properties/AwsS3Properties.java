package com.devin.s3.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 2025/4/16 15:12.
 *
 * <p>
 *     AWS S3 配置属性
 * </p>
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 */
@Data
@ConfigurationProperties(prefix = "aws.s3")
public class AwsS3Properties {

    /**
     * AWS S3 访问密钥.
     */
    private String accessKey;

    /**
     * AWS S3 密钥.
     */
    private String secretKey;

    /**
     * AWS S3 区域.
     */
    private String region;

    /**
     * 代理配置.
     * @see Proxy
     */
    private Proxy proxy;

    @Data
    public static class Proxy {
        /**
         * 默认本地代理地址.
         */
        private static final String DEFAULT_PROXY_HOST = "127.0.0.1";

        /**
         * 默认本地代理端口.
         */
        private static final String DEFAULT_PROXY_PORT = "7890";

        /**
         * 代理服务器ip地址，默认本地代理.
         */
        private String host = DEFAULT_PROXY_HOST;

        /**
         * 代理服务器端口.
         */
        private String port = DEFAULT_PROXY_PORT;
    }
}
