package com.devin.sso.service;

import com.devin.sso.entity.IdTokenClaims;
import com.devin.sso.entity.User;

/**
 * 2025/9/19 10:51.
 *
 * <p></p>
 *
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 */
public interface Oauth2Service {

    /**
     * 生成Gitee登录URL.
     *
     * @return Gitee登录URL
     */
    String generateGiteeLoginURL();

    /**
     * 换取Gitee的token并验证.
     * @param code Gitee的code
     * @return IdTokenClaims
     */
    IdTokenClaims exchangeCodeForTokensAndVerify(String code);

    /**
     * 登录.
     * @param claims IdTokenClaims
     * @return  User
     */
    User login(IdTokenClaims claims);
}
