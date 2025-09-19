package com.devin.sso.controller;

import com.devin.common.utils.ApiResult;
import com.devin.sso.common.properties.SsoProperty;
import com.devin.sso.entity.IdTokenClaims;
import com.devin.sso.entity.User;
import com.devin.sso.service.Oauth2Service;
import com.devin.sso.service.SamlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;

/**
 * 2025/9/17 21:21.
 *
 * <p>
 *     单点登录方案
 * </p>
 *
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/sso")
@Tag(name = "SSO", description = "单点登录方案")
public class SsoController {

    private final SamlService samlService;

    private final Oauth2Service oauth2Service;

    private final SsoProperty ssoProperty;
    private final RestTemplate restTemplate;

    /**
     * SAML登录.
     * @param request 请求
     * @param response 响应
     */
    @Operation(summary = "SAML登录")
    @GetMapping("/saml/login")
    public void samlLogin(
           final HttpServletRequest request,
           final HttpServletResponse response
    ) throws IOException {
        String idPLoginURL = samlService.generateIdPLoginURL();
        response.sendRedirect(idPLoginURL);
    }

    /**
     * SAML单点登录callback.
     * @param request 请求
     * @param response 响应
     * @return ApiResult
     * @throws IOException IO异常
     */
    @Operation(summary = "SAML单点登录callback")
    @PostMapping("/saml/callback")
    public ApiResult<Boolean> samlCallback(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        String samlResponse = request.getParameter("SAMLResponse");
        samlService.ssoLogin(samlResponse);
        response.sendRedirect(ssoProperty.getSaml().getEntityId());
        return ApiResult.success();
    }

    /**
     * OAuth登录.
     * @param request 请求
     * @param response 响应
     * @return ApiResult
     */
    @GetMapping("/oauth/login")
    @Operation(summary = "OAuth登录")
    public void oauthGiteeLogin(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        SsoProperty.OAuth2Property oauth2Config = ssoProperty.getOauth2();
        String idPUrl = oauth2Config.getIdPUrl();
        Map<String, Object> res = restTemplate.getForObject(idPUrl, Map.class);

        // 从返回的 JSON 文档中解析并存储我们需要的端点
        String issuerUri = (String) res.get("issuer");
        String authorizationUri = (String) res.get("authorization_endpoint");
        String tokenUri = (String) res.get("token_endpoint");
        String jwkSetUri = (String) res.get("jwks_uri");
        oauth2Config.setIssuerUri(issuerUri);
        oauth2Config.setAuthorizationUri(authorizationUri);
        oauth2Config.setTokenUri(tokenUri);
        oauth2Config.setJwkSetUri(jwkSetUri);

        String url = UriComponentsBuilder
                .fromUriString(oauth2Config.getAuthorizationUri())
                .queryParam("response_type", "code")
                .queryParam("client_id", "410b171f1454f505bf7482e9f15d52c717b33e37f42d1d43a10c735e233b4989")
                .queryParam("scope", oauth2Config.getScope())
                .queryParam("redirect_uri", "http://192.168.110.27:8080/sso/oauth/callback")
                .queryParam("state", generateState())
                .build().toUriString();

        response.sendRedirect(url);
    }

    @GetMapping("/oauth/callback")
    @Operation(summary = "OAuth callback")
    public ApiResult<Boolean> oauthCallback(@RequestParam("code") final String code,
                                            @RequestParam("state") final String state,
                                            final HttpServletRequest request,
                                            final HttpServletResponse response) throws IOException {
        // 验证 state
//        String sessionState = (String) request.getSession().getAttribute("OAUTH2_STATE");
//        SsoProperty.OAuth2Property oauth2Config = ssoProperty.getOauth2();

        // 交换并验证令牌
        IdTokenClaims claims = oauth2Service.exchangeCodeForTokensAndVerify(code);
        User user = oauth2Service.login(claims);

//        Subject subject = SecurityUtils.getSubject();
//        UsernamePasswordToken token = new UsernamePasswordToken(user.getId().toString(), "admin");
//        Session session = subject.getSession();
//        session.setTimeout(86400000L * 30);
//        subject.login(token);
//        response.addHeader("auth-token", subject.getSession().getId().toString());
//        response.sendRedirect(oauth2Config.getHomeUrl());

//        request.getSession().removeAttribute("OAUTH2_STATE");

        return ApiResult.success();
    }

    private String generateState() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
