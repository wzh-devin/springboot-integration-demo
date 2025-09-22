package com.devin.sso.controller;

import com.devin.common.utils.ApiResult;
import com.devin.sso.common.configuration.Oauth2Config;
import com.devin.sso.entity.IdTokenClaims;
import com.devin.sso.entity.User;
import com.devin.sso.service.DemoOauth2Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
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
 * 2025/9/22 12:31.
 *
 * <p></p>
 *
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/")
@Tag(name = "SSO", description = "单点登录方案")
public class SsoProController {

    private final DemoOauth2Service oauth2Service;

    private final RestTemplate restTemplate;

    private Oauth2Config oauth2Config;

    @PostConstruct
    public void init() {
        oauth2Config = Oauth2Config.builder()
                .clientId("410b171f1454f505bf7482e9f15d52c717b33e37f42d1d43a10c735e233b4989")
                .clientSecret("5c0a1dc6784ca0b8f3f473c8ea8a35819b6cca21c6b34fcaa0695457bf49982f")
                .redirectUri("http://localhost:8080/oauth2/callback")
                .discoveryUrl("http://localhost:14008/.well-known/openid-configuration")
                .build();
    }

    @GetMapping("/oauth2/login")
    public void oauth2Login(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws IOException {
        // 获取登录配置
        Map<String, Object> configMap = restTemplate.getForObject(oauth2Config.getDiscoveryUrl(), Map.class);

        oauth2Config.setIssuer((String) configMap.get("issuer"));
        oauth2Config.setAuthorizationEndpoint((String) configMap.get("authorization_endpoint"));
        oauth2Config.setTokenEndpoint((String) configMap.get("token_endpoint"));
        oauth2Config.setJwkEndpoint((String) configMap.get("jwks_uri"));

        // 构建登录URL
        String loginUrl = UriComponentsBuilder
                .fromUriString(oauth2Config.getAuthorizationEndpoint())
                .queryParam("client_id", oauth2Config.getClientId())
                .queryParam("response_type", "code")
                .queryParam("redirect_uri", oauth2Config.getRedirectUri())
                .queryParam("scope", "profile email")
                .queryParam("state", generateState())
                .queryParam("response_mode", "query")
                .build()
                .toUriString();

        response.sendRedirect(loginUrl);
    }

    @GetMapping("/oauth2/callback")
    @Operation(summary = "OAuth callback")
    public ApiResult<Boolean> oauthCallback(@RequestParam("code") final String code,
                                            @RequestParam("state") final String state,
                                            final HttpServletRequest request,
                                            final HttpServletResponse response) throws IOException {
        // 验证 state
//        String sessionState = (String) request.getSession().getAttribute("OAUTH2_STATE");
//        SsoProperty.OAuth2Property oauth2Config = ssoProperty.getOauth2();

        IdTokenClaims claims = oauth2Service.exchangeCodeForTokensOAuth2Style(code, oauth2Config);

        // 交换并验证令牌
//        IdTokenClaims claims = oauth2Service.exchangeCodeForTokensAndVerify(code);
//        User user = oauth2Service.login(claims);

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
