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
    public ApiResult<Boolean> samlCallback(final HttpServletRequest request, final HttpServletResponse response, @RequestParam("samlResponse") final String samlResponse) throws IOException {
//        String samlResponse = "PHNhbWxwOlJlc3BvbnNlIElEPSJfMDZhOGM0NTAtOGI4Ny00YjU5LTljYTctZjcwZGYyMmI1OWMwIiBWZXJzaW9uPSIyLjAiIElzc3VlSW5zdGFudD0iMjAyNS0wOS0yNFQxMDo0ODo1Ny4xMDdaIiBEZXN0aW5hdGlvbj0iaHR0cHM6Ly9yaXNlc3VhdC5jYXBpdGFsYW5kLmNvbS5jbi9jbWEvYXBwbG9naW4uYXNweCIgQ29uc2VudD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmNvbnNlbnQ6dW5zcGVjaWZpZWQiIEluUmVzcG9uc2VUbz0iX2IyM2RhZWIxLTEzMzMtNDYxYi04YmRjLTA1NmJkODJkOTIyMSIgeG1sbnM6c2FtbHA9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpwcm90b2NvbCI+PElzc3VlciB4bWxucz0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmFzc2VydGlvbiI+aHR0cDovL2dhbGF4eS5jYXBpdGFsYW5kLmNvbS9hZGZzL3NlcnZpY2VzL3RydXN0PC9Jc3N1ZXI+PHNhbWxwOlN0YXR1cz48c2FtbHA6U3RhdHVzQ29kZSBWYWx1ZT0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOnN0YXR1czpTdWNjZXNzIiAvPjwvc2FtbHA6U3RhdHVzPjxBc3NlcnRpb24gSUQ9Il9lZDYxYWQwZi1lNzMzLTQzZDgtODNhNS01OTE5N2E0NjIxMmMiIElzc3VlSW5zdGFudD0iMjAyNS0wOS0yNFQxMDo0ODo1Ny4xMDdaIiBWZXJzaW9uPSIyLjAiIHhtbG5zPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXNzZXJ0aW9uIj48SXNzdWVyPmh0dHA6Ly9nYWxheHkuY2FwaXRhbGFuZC5jb20vYWRmcy9zZXJ2aWNlcy90cnVzdDwvSXNzdWVyPjxkczpTaWduYXR1cmUgeG1sbnM6ZHM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyMiPjxkczpTaWduZWRJbmZvPjxkczpDYW5vbmljYWxpemF0aW9uTWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIiAvPjxkczpTaWduYXR1cmVNZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzA0L3htbGRzaWctbW9yZSNyc2Etc2hhMjU2IiAvPjxkczpSZWZlcmVuY2UgVVJJPSIjX2VkNjFhZDBmLWU3MzMtNDNkOC04M2E1LTU5MTk3YTQ2MjEyYyI+PGRzOlRyYW5zZm9ybXM+PGRzOlRyYW5zZm9ybSBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyNlbnZlbG9wZWQtc2lnbmF0dXJlIiAvPjxkczpUcmFuc2Zvcm0gQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzEwL3htbC1leGMtYzE0biMiIC8+PC9kczpUcmFuc2Zvcm1zPjxkczpEaWdlc3RNZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzA0L3htbGVuYyNzaGEyNTYiIC8+PGRzOkRpZ2VzdFZhbHVlPnl4aDFJQ1R5eUsxOGZiZzJuam0zZnRUUnhpYVpxamdGOUJFSkFXWmxQMXc9PC9kczpEaWdlc3RWYWx1ZT48L2RzOlJlZmVyZW5jZT48L2RzOlNpZ25lZEluZm8+PGRzOlNpZ25hdHVyZVZhbHVlPll3NFpLWUdnelhGNTczdjVrZUlvU0dmM2VrMWxKRU1yck9meU5YaHEvektZd2hrbWdmd1dxc20rWWR6dGYwYWFybTFyWTZaa2tXUktVekpYZWs1aHZHYkNLY2pqZEZ5S0dxeVAxd3pwRG5WS1dWcEZpNWo1K092Y0pMbVNEN0JCbkdGa0RPM0RuSG1lQm1tMjZ1ZHdjY3pxdy9Nd0JkV3NSNVR3OEt2WEs4MUdFY1NYdkczQmZ0NzlyTDlSdGNVMzBJTWQvbkdaUzhqOWk2L0hHL241SUI1c2FGaFN1allTVHEwc090ZnBpb3UwdFU5K1g5M2FMWWhlcU81My9SaHV2SVFDNHBHbi82blA0eGpDRUJzR2hyMFVCUmpSTzBWNnkxLyt1RE9HSDNyQnhSN25VdjQxNjBDNmdVZzJvcEVtaFlNd0Nubzd4aDM3R3B5RW5mSWN2dz09PC9kczpTaWduYXR1cmVWYWx1ZT48S2V5SW5mbyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnIyI+PGRzOlg1MDlEYXRhPjxkczpYNTA5Q2VydGlmaWNhdGU+TUlJQzVqQ0NBYzZnQXdJQkFnSVFlVmc0eUtHdjI0Rk9GRzYzM20yWXVUQU5CZ2txaGtpRzl3MEJBUXNGQURBdk1TMHdLd1lEVlFRREV5UkJSRVpUSUZOcFoyNXBibWNnTFNCbllXeGhlSGt1WTJGd2FYUmhiR0Z1WkM1amIyMHdIaGNOTWpFd01USXdNVE16TnpNeldoY05NekV3TVRFNE1UTXpOek16V2pBdk1TMHdLd1lEVlFRREV5UkJSRVpUSUZOcFoyNXBibWNnTFNCbllXeGhlSGt1WTJGd2FYUmhiR0Z1WkM1amIyMHdnZ0VpTUEwR0NTcUdTSWIzRFFFQkFRVUFBNElCRHdBd2dnRUtBb0lCQVFEZ2RkZktSalNjbWZiemFTd0E5V1VxcVpqTjBlUnd5QUxvbDN4c1BMRldFTEVaME9KbDl0Tk5DVjBXcXBhY2JKNlJCVnRrNDRGWVFjYU1VbHk3blJxQTh5bXVMbnd5bFFlNHpVbUlDQ3JtM21Wdm51UTZHb0pheWpUQW9YTmppalFpS1hERmNIZnlOUUlwYnE1bXMxbUtTWkhXaEMzNkpWdEloNHJkQWZwSS9EZTI2Q1R3WUJ6cjNlYmpCaHZYN2xSclQ3azE0STNwTEJTUjhrRXBreE43Zmx4STZFUUNUaTdTbnBiQUxWZElkNE9xQUhxWmJWZWRIMHdDUEtWTkZoNlJlNFpoQlZyTmFFa29XSVBDK1diQ1BHR3huODloV1JhMjd4clZDK3pib2FweVR3Zzl4d3FkMTBqV0JaSDZac0VVRHBQNktyVlBYNFozMjJNTitxR1RBZ01CQUFFd0RRWUpLb1pJaHZjTkFRRUxCUUFEZ2dFQkFNN3E2MUgwbjdOT0FZSHZ0U1FoNlhteXJ6MTUxdS9NWmVVNzllV1U5YUh6bXo4bkcrcUNDdjZKMkJrbkI2d1k2SXIyaFE5WE1TVE9IVW1mMm5UVGRKZnV5U2ZxV3VmMUtRa0hRZW44TG0vSlhleUVZdXhvNExqYlRydEN0WTBQM2lMNUpnak9reHBoV2NlVHBqQ1ZUOG9vODVTK1pvVmVxR2c2R2I5cUF2SERmaWFLSWtBRUFwMEhwS2hwMnRWUGFGamRHZnVtZEJaa25XdnNjMzdVSklwUXArVmgrMm5vam1lSmR3ejdYVkVRZ1hQajNyMTkzNVMySWl1WE9ReEo1dHhGQjhJRXNrdGg0WDNZc0Z6b1BXNjEzMHdlNXR0S2QzQnIrMGd3UThvWkQ4QjgvWlEvZGJSRW5IUXBlRWZEb2NNUmp5M0dLeEhpRTg3aktvdW5Eb2M9PC9kczpYNTA5Q2VydGlmaWNhdGU+PC9kczpYNTA5RGF0YT48L0tleUluZm8+PC9kczpTaWduYXR1cmU+PFN1YmplY3Q+PE5hbWVJRD52cl9odWEuamlheWk8L05hbWVJRD48U3ViamVjdENvbmZpcm1hdGlvbiBNZXRob2Q9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpjbTpiZWFyZXIiPjxTdWJqZWN0Q29uZmlybWF0aW9uRGF0YSBJblJlc3BvbnNlVG89Il9iMjNkYWViMS0xMzMzLTQ2MWItOGJkYy0wNTZiZDgyZDkyMjEiIE5vdE9uT3JBZnRlcj0iMjAyNS0wOS0yNFQxMDo1Mzo1Ny4xMDdaIiBSZWNpcGllbnQ9Imh0dHBzOi8vcmlzZXN1YXQuY2FwaXRhbGFuZC5jb20uY24vY21hL2FwcGxvZ2luLmFzcHgiIC8+PC9TdWJqZWN0Q29uZmlybWF0aW9uPjwvU3ViamVjdD48Q29uZGl0aW9ucyBOb3RCZWZvcmU9IjIwMjUtMDktMjRUMTA6NDg6NTcuMTA3WiIgTm90T25PckFmdGVyPSIyMDI1LTA5LTI0VDExOjQ4OjU3LjEwN1oiPjxBdWRpZW5jZVJlc3RyaWN0aW9uPjxBdWRpZW5jZT5odHRwczovL3Jpc2VzdWF0LmNhcGl0YWxhbmQuY29tLmNuL2NtYS9hcHBsb2dpbi5hc3B4PC9BdWRpZW5jZT48L0F1ZGllbmNlUmVzdHJpY3Rpb24+PC9Db25kaXRpb25zPjxBdXRoblN0YXRlbWVudCBBdXRobkluc3RhbnQ9IjIwMjUtMDktMjRUMTA6NDg6MjQuNDEwWiIgU2Vzc2lvbkluZGV4PSJfZWQ2MWFkMGYtZTczMy00M2Q4LTgzYTUtNTkxOTdhNDYyMTJjIj48QXV0aG5Db250ZXh0PjxBdXRobkNvbnRleHRDbGFzc1JlZj51cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YWM6Y2xhc3NlczpQYXNzd29yZFByb3RlY3RlZFRyYW5zcG9ydDwvQXV0aG5Db250ZXh0Q2xhc3NSZWY+PC9BdXRobkNvbnRleHQ+PC9BdXRoblN0YXRlbWVudD48L0Fzc2VydGlvbj48L3NhbWxwOlJlc3BvbnNlPg==";
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
