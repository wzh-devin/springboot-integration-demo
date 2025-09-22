package com.devin.sso.service;

import com.devin.sso.entity.IdTokenClaims;
import com.devin.sso.entity.User;

/**
 * 2025/9/17 21:23.
 *
 * <p></p>
 *
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 */
public interface SsoService {
    void initializeAzureAdfsEndpoints();

    String generateAzureAdfsLoginUrl();

    IdTokenClaims exchangeCodeForTokensAndVerify(String code);

    User login(IdTokenClaims claims);

    String generateOAuth2LoginUrl();

    IdTokenClaims exchangeCodeForTokensOAuth2Style(String code);
}
