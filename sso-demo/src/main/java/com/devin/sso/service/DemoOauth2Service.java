package com.devin.sso.service;

import com.devin.sso.common.configuration.Oauth2Config;
import com.devin.sso.entity.IdTokenClaims;

/**
 * 2025/9/22 12:31.
 *
 * <p></p>
 *
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 */
public interface DemoOauth2Service {
    IdTokenClaims exchangeCodeForTokensOAuth2Style(String code, Oauth2Config oauth2Config);
}
