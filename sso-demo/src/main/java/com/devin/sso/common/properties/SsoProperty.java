package com.devin.sso.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 2025/9/18 11:22.
 *
 * <p></p>
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 */
@Data
@ConfigurationProperties(prefix = "sso")
public class SsoProperty {

    /**
     * SAML服务配置.
     */
    private SamlProperty saml;

    private OAuth2Property oauth2;

    @Data
    public static class SamlProperty {

        /**
         * IdP服务应用配置.
         */
        private String idpEntityId;

        /**
         * IdP服务登录地址.
         */
        private String idpUrl;

        /**
         * SP服务应用配置.
         */
        private String entityId;

        /**
         * SP服务回调地址.
         */
        private String acsUrl;

        /**
         * SP服务公钥.
         */
        private String publicKey;
    }

    @Data
    public static class OAuth2Property {
        private String clientId;

        private String clientSecret;

        private String redirectUrl;

        private String homeUrl;

        private String scope;

        private String idPUrl;

        private String issuerUri;

        private String authorizationUri;

        private String tokenUri;

        private String jwkSetUri;
    }
}
