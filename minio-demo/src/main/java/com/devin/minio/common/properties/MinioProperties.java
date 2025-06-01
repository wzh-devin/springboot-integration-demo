package com.devin.minio.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 2025/6/1 15:11.
 *
 * <p>
 * minio配置类
 * </p>
 *
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 */
@Data
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    /**
     * minio服务地址.
     */
    private String endpoint;

    /**
     * AccessKey.
     */
    private String accessKey;

    /**
     * SecretKey.
     */
    private String secretKey;

}
