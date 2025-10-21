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
public class SsoProperty {

    /**
     * SAML服务配置.
     */
    private SamlProperty saml;

    private OAuth2Property oauth2;

    private AzureAdfsProperty azureAdfs;

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

    @Data
    public static class AzureAdfsProperty {
        /**
         * Azure ADFS 租户 ID.
         */
        private String tenantId;
        
        /**
         * Azure ADFS 应用程序（客户端）ID.
         */
        private String clientId;
        
        /**
         * Azure ADFS 客户端密钥.
         */
        private String clientSecret;
        
        /**
         * 重定向 URI.
         */
        private String redirectUri;
        
        /**
         * 作用域.
         */
        private String scope = "openid profile email";
        
        /**
         * Azure ADFS 实例 URL.
         */
        private String authority;
        
        /**
         * 授权端点.
         */
        private String authorizationEndpoint;
        
        /**
         * 令牌端点.
         */
        private String tokenEndpoint;
        
        /**
         * JWK Set URI.
         */
        private String jwkSetUri;
        
        /**
         * 发行者 URI.
         */
        private String issuer;
        
        /**
         * 登录成功后的主页 URL.
         */
        private String homeUrl;
    }
}
