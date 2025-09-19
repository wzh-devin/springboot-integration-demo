package com.devin.sso.service;

import com.devin.sso.entity.User;

/**
 * 2025/9/17 22:12.
 *
 * <p>
 * SAML协议服务
 * </p>
 *
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 */
public interface SamlService {

    /**
     * 生成idP登录的URL地址.
     *
     * @return URL
     */
    String generateIdPLoginURL();

    /**
     * SSO登录.
     * @param samlResponse SAML响应
     * @return  User
     */
    User ssoLogin(String samlResponse);
}
