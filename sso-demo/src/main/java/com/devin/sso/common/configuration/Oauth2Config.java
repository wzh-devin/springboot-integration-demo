package com.devin.sso.common.configuration;

import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * 2025/9/22 12:41.
 *
 * <p></p>
 *
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
public class Oauth2Config {

    // 服务发现地址
    private String discoveryUrl;

    private String clientId;

    private String clientSecret;

    private String redirectUri;

    private String issuer;

    private String authorizationEndpoint;

    private String tokenEndpoint;

    private String jwkEndpoint;
}
