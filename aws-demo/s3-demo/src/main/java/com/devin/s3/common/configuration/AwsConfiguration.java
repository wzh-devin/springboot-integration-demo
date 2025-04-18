package com.devin.s3.common.configuration;

import com.devin.s3.common.exception.VerificationException;
import com.devin.s3.common.properties.AwsS3Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.http.apache.ProxyConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import java.net.URI;
import java.time.Duration;
import java.util.Objects;

/**
 * 2025/4/16 15:12.
 *
 * <p>
 *     AWS配置类
 * </p>
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(AwsS3Properties.class)
public class AwsConfiguration {

    /**
     * 初始化S3Client.
     * @param properties 配置文件
     * @return s3Client
     */
    @Bean
    public S3Client s3Client(final AwsS3Properties properties) {
        S3ClientBuilder s3ClientBuilder = S3Client.builder()
                .region(Region.of(properties.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(credentials(properties.getAccessKey(), properties.getSecretKey())))
                .forcePathStyle(true);

        // 判断是否需要代理
        if (Objects.nonNull(properties.getProxy())) {
            AwsS3Properties.Proxy proxy = properties.getProxy();

            if (Objects.isNull(proxy.getPort())) {
                throw new VerificationException("请填写代理端口...");
            }

            // 配置代理
            ProxyConfiguration proxyConfiguration = ProxyConfiguration.builder()
                    .endpoint(URI.create(String.format("http://%s:%s", proxy.getHost(), proxy.getPort())))
                    .build();

            // 为S3客户端添加代理
            s3ClientBuilder.httpClient(ApacheHttpClient.builder()
                    .connectionTimeout(Duration.ofMinutes(1))
                    .socketTimeout(Duration.ofMinutes(1))
                    .proxyConfiguration(proxyConfiguration)
                    .build());
        }

        return s3ClientBuilder.build();
    }

    /**
     * 配置预签名URL.
     * @param properties 配置文件
     * @return s3Presigner
     */
    @Bean
    public S3Presigner s3Presigner(final AwsS3Properties properties) {
        return S3Presigner.builder()
                .region(Region.of(properties.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(credentials(properties.getAccessKey(), properties.getSecretKey())))
                .build();
    }

    /**
     * 验证处理函数.
     * @param accessKey 访问密钥
     * @param secretKey 密钥
     * @return awsBasicCredentials
     */
    private AwsBasicCredentials credentials(final String accessKey, final String secretKey) {
        if (Objects.isNull(accessKey) || Objects.isNull(secretKey)) {
            throw new VerificationException("请填写S3必须的AccessKey和SecretKey参数...");
        }
        return AwsBasicCredentials.create(accessKey, secretKey);
    }
}
