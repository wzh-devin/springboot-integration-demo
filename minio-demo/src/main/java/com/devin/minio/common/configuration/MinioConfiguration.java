package com.devin.minio.common.configuration;

import com.devin.minio.common.properties.MinioProperties;
import io.minio.MinioClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 2025/6/1 15:17.
 *
 * <p></p>
 *
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 */
@Configuration
@EnableConfigurationProperties(MinioProperties.class)
class MinioConfiguration {

    @Bean
    public MinioClient minioClient(final MinioProperties properties) {
        return MinioClient.builder()
                .endpoint("http://%s".formatted(properties.getEndpoint()))
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();
    }
}
