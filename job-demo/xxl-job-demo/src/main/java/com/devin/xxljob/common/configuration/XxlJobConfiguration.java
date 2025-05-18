package com.devin.xxljob.common.configuration;

import com.devin.xxljob.common.properties.XxlJobProperties;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 2025/5/16 14:56.
 *
 * <p></p>
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(XxlJobProperties.class)
public class XxlJobConfiguration {

    @Bean
    public XxlJobSpringExecutor xxlJobExecutor(final XxlJobProperties xxlJobProperties) {
        log.info(">>>>>>>>>>> xxl-job config init.");
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        XxlJobProperties.Admin admin = xxlJobProperties.getAdmin();
        if (admin != null && StringUtils.isNotEmpty(admin.getAddress())) {
            xxlJobSpringExecutor.setAdminAddresses(admin.getAddress());
        }
        XxlJobProperties.Executor executor = xxlJobProperties.getExecutor();
        xxlJobSpringExecutor.setAccessToken(xxlJobProperties.getAccessToken());
        if (executor != null) {
            xxlJobSpringExecutor.setIp(executor.getIp());
            xxlJobSpringExecutor.setAppname(executor.getAppName());
            xxlJobSpringExecutor.setPort(executor.getPort());
            xxlJobSpringExecutor.setLogPath(executor.getLogPath());
            xxlJobSpringExecutor.setLogRetentionDays(
                    executor.getLogRetentionDays()
            );
        }

        log.info(">>>>>>>>>>> xxl-job config end.");
        return xxlJobSpringExecutor;
    }
}

