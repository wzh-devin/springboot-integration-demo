package com.devin.sso.common.configuration;

import com.devin.sso.common.properties.SsoProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.opensaml.DefaultBootstrap;
import org.opensaml.xml.ConfigurationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 2025/9/18 11:00.
 *
 * <p></p>
 *
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 */
@Data
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(SsoProperty.class)
public class SsoConfiguration {

    /**
     * Initialize OpenSAML library.
     *
     * @return true if initialization was successful
     */
    @Bean
    public static boolean initializeOpenSAML() {
        try {
            DefaultBootstrap.bootstrap();
            return true;
        } catch (ConfigurationException e) {
            throw new IllegalStateException("Failed to initialize OpenSAML library", e);
        }
    }
}
