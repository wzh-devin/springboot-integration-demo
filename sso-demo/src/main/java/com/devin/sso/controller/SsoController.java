package com.devin.sso.controller;

import com.devin.common.utils.ApiResult;
import com.devin.sso.common.properties.SsoProperty;
import com.devin.sso.entity.IdTokenClaims;
import com.devin.sso.entity.User;
import com.devin.sso.service.Oauth2Service;
import com.devin.sso.service.SamlService;
import com.devin.sso.service.SsoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final SsoService ssoService;

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
    @GetMapping("/oauth2/login")
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
                .queryParam("client_id", oauth2Config.getClientId())
                .queryParam("scope", oauth2Config.getScope())
                .queryParam("redirect_uri", oauth2Config.getRedirectUrl())
                .queryParam("state", generateState())
                .build().toUriString();

        response.sendRedirect(url);
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

    /**
     * Azure ADFS 登录.
     * @param request 请求
     * @param response 响应
     * @throws IOException IO异常
     */
    @Operation(summary = "Azure ADFS 登录")
    @GetMapping("/azure-adfs/login")
    public void azureAdfsLogin(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws IOException {
        // 初始化 Azure ADFS 端点配置
        ssoService.initializeAzureAdfsEndpoints();
        
        // 生成登录 URL 并重定向
        String loginUrl = ssoService.generateAzureAdfsLoginUrl();
        response.sendRedirect(loginUrl);
    }

    /**
     * Azure ADFS 单点登录回调.
     * @param code 授权码
     * @param state 状态参数
     * @param request 请求
     * @param response 响应
     * @return ApiResult
     * @throws IOException IO异常
     */
    @Operation(summary = "Azure ADFS 单点登录回调")
    @GetMapping("/azure-adfs/callback")
    public ApiResult<Boolean> azureAdfsCallback(
            @RequestParam("code") final String code,
            @RequestParam(value = "state", required = false) final String state,
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws IOException {
        try {
            // 验证状态参数（在实际应用中，应该验证 state 参数防止 CSRF 攻击）
            if (state == null || state.isEmpty()) {
                log.warn("State parameter is missing in Azure ADFS callback");
            }
            
            // 交换授权码获取令牌并验证
            IdTokenClaims claims = ssoService.exchangeCodeForTokensAndVerify(code);
            
            // 用户登录
            User user = ssoService.login(claims);
            
            // 在实际应用中，这里应该设置用户会话
            // 例如：设置 session 或 JWT token
            // request.getSession().setAttribute("user", user);
            
            log.info("Azure ADFS login successful for user: {}", user.getUsername());
            
            // 重定向到主页
            SsoProperty.AzureAdfsProperty azureConfig = ssoProperty.getAzureAdfs();
            if (azureConfig.getHomeUrl() != null) {
                response.sendRedirect(azureConfig.getHomeUrl());
            }
            
            return ApiResult.success();
            
        } catch (Exception e) {
            log.error("Azure ADFS login failed: {}", e.getMessage(), e);
            return ApiResult.fail(500, "登录失败: " + e.getMessage());
        }
    }

    /**
     * 获取 Azure ADFS 配置信息.
     * @return Azure ADFS 配置信息
     */
    @Operation(summary = "获取 Azure ADFS 配置信息")
    @GetMapping("/azure-adfs/config")
    public ApiResult<Map<String, String>> getAzureAdfsConfig() {
        try {
            ssoService.initializeAzureAdfsEndpoints();
            SsoProperty.AzureAdfsProperty azureConfig = ssoProperty.getAzureAdfs();
            
            Map<String, String> configInfo = new java.util.HashMap<>();
            configInfo.put("authority", azureConfig.getAuthority());
            configInfo.put("clientId", azureConfig.getClientId());
            configInfo.put("redirectUri", azureConfig.getRedirectUri());
            configInfo.put("scope", azureConfig.getScope());
            configInfo.put("authorizationEndpoint", azureConfig.getAuthorizationEndpoint());
            configInfo.put("tokenEndpoint", azureConfig.getTokenEndpoint());
            configInfo.put("jwkSetUri", azureConfig.getJwkSetUri());
            configInfo.put("issuer", azureConfig.getIssuer());
            
            return ApiResult.success(configInfo);
        } catch (Exception e) {
            log.error("Failed to get Azure ADFS config: {}", e.getMessage(), e);
            return ApiResult.fail(500, "获取配置失败: " + e.getMessage());
        }
    }

    /**
     * 纯 OAuth2 登录（不使用 OIDC）.
     * @param request 请求
     * @param response 响应
     * @throws IOException IO异常
     */
    @Operation(summary = "纯 OAuth2 登录（不使用 OIDC）")
    @GetMapping("/oauth2-pure/login")
    public void oauth2PureLogin(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws IOException {
        // 初始化 Azure ADFS 端点配置
        ssoService.initializeAzureAdfsEndpoints();
        
        // 生成纯 OAuth2 登录 URL（不包含 openid scope）
        String loginUrl = ssoService.generateOAuth2LoginUrl();
        response.sendRedirect(loginUrl);
    }

    /**
     * 纯 OAuth2 回调（使用 access_token 调用用户信息端点）.
     * @param code 授权码
     * @param state 状态参数
     * @param request 请求
     * @param response 响应
     * @return ApiResult
     * @throws IOException IO异常
     */
    @Operation(summary = "纯 OAuth2 回调")
    @GetMapping("/oauth2-pure/callback")
    public ApiResult<Boolean> oauth2PureCallback(
            @RequestParam("code") final String code,
            @RequestParam(value = "state", required = false) final String state,
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws IOException {
        try {
            log.info("Processing pure OAuth2 callback (no OIDC)");
            
            // 使用纯 OAuth2 流程：access_token + userinfo endpoint
            IdTokenClaims claims = ssoService.exchangeCodeForTokensOAuth2Style(code);
            
            // 用户登录
            User user = ssoService.login(claims);
            
            log.info("Pure OAuth2 login successful for user: {}", user.getUsername());
            
            // 重定向到主页
            SsoProperty.AzureAdfsProperty azureConfig = ssoProperty.getAzureAdfs();
            if (azureConfig.getHomeUrl() != null) {
                response.sendRedirect(azureConfig.getHomeUrl());
            }
            
            return ApiResult.success();
            
        } catch (Exception e) {
            log.error("Pure OAuth2 login failed: {}", e.getMessage(), e);
            return ApiResult.fail(500, "纯 OAuth2 登录失败: " + e.getMessage());
        }
    }
}
