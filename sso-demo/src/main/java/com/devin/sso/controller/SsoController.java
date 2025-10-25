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
        samlService.ssoLogin("PHNhbWxwOlJlc3BvbnNlIElEPSJfMWEwNjA0MmEtMzhkMi00NDE2LTg4NWItZTU1MzFlODVhYTM3IiBWZXJzaW9uPSIyLjAiIElzc3VlSW5zdGFudD0iMjAyNS0xMC0yMVQwODozNzozMC4zNDVaIiBEZXN0aW5hdGlvbj0iaHR0cHM6Ly9seW54YWkuY2FwaXRhbGFuZC5jb20uY24vdHlwZWtleS9hcGkvdjEvc3NvL3NhbWwvY2FsbGJhY2siIENvbnNlbnQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpjb25zZW50OnVuc3BlY2lmaWVkIiBJblJlc3BvbnNlVG89Il9mMmY1MGU0My1lNGJiLTQyZDctYWEzMC1mZDYyYTQ1NjVhZjkiIHhtbG5zOnNhbWxwPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6cHJvdG9jb2wiPjxJc3N1ZXIgeG1sbnM9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphc3NlcnRpb24iPmh0dHA6Ly9nYWxheHkuY2FwaXRhbGFuZC5jb20vYWRmcy9zZXJ2aWNlcy90cnVzdDwvSXNzdWVyPjxzYW1scDpTdGF0dXM+PHNhbWxwOlN0YXR1c0NvZGUgVmFsdWU9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpzdGF0dXM6U3VjY2VzcyIgLz48L3NhbWxwOlN0YXR1cz48QXNzZXJ0aW9uIElEPSJfNzJmMjYwMjktOGRjZi00MmI1LWI2NzctM2FkMjBjYTExYWViIiBJc3N1ZUluc3RhbnQ9IjIwMjUtMTAtMjFUMDg6Mzc6MzAuMzQ1WiIgVmVyc2lvbj0iMi4wIiB4bWxucz0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmFzc2VydGlvbiI+PElzc3Vlcj5odHRwOi8vZ2FsYXh5LmNhcGl0YWxhbmQuY29tL2FkZnMvc2VydmljZXMvdHJ1c3Q8L0lzc3Vlcj48ZHM6U2lnbmF0dXJlIHhtbG5zOmRzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjIj48ZHM6U2lnbmVkSW5mbz48ZHM6Q2Fub25pY2FsaXphdGlvbk1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMTAveG1sLWV4Yy1jMTRuIyIgLz48ZHM6U2lnbmF0dXJlTWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8wNC94bWxkc2lnLW1vcmUjcnNhLXNoYTI1NiIgLz48ZHM6UmVmZXJlbmNlIFVSST0iI183MmYyNjAyOS04ZGNmLTQyYjUtYjY3Ny0zYWQyMGNhMTFhZWIiPjxkczpUcmFuc2Zvcm1zPjxkczpUcmFuc2Zvcm0gQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjZW52ZWxvcGVkLXNpZ25hdHVyZSIgLz48ZHM6VHJhbnNmb3JtIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIiAvPjwvZHM6VHJhbnNmb3Jtcz48ZHM6RGlnZXN0TWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8wNC94bWxlbmMjc2hhMjU2IiAvPjxkczpEaWdlc3RWYWx1ZT5UR2tNWnFNUXZoYWhuUHEzVDBwZDQzdHQrZENKUHB2MVBGNFBFZFVSRTdjPTwvZHM6RGlnZXN0VmFsdWU+PC9kczpSZWZlcmVuY2U+PC9kczpTaWduZWRJbmZvPjxkczpTaWduYXR1cmVWYWx1ZT5hK2dmWFhOSTVLMHQ3VmJ0R3AzSmdJWTRJeC96cGtBLzdJeW1GWURoanRFRFdPR3FwR2xwTmltRXpZTzRoUG5xSXpYRXdHZDhCRnFPMGtrWVhhekxZeTJhQXMxWTVETXRRN2NKdXVMaUlBVDlJTjdnVUFrN2o5SzRROXhsMHJMamV4RWNraDdURTJoS3p4SnA3Qkx0N0tBYzBBQWpVSm83NTE5bDNEaEdUVlRDVUxoaWVkakQzam1MZ0xSOG5Nb1ZTaXYxV1Z0alY3djlYZTgrWTByb3NvM1crcndMSzBZV2VBdmdWYUVaYnJ0U0lUTjBwenJ1RU1ycTVqME9xUHlyVnhvRDAvQ0hHMlhKREpkd2ZFUncyRHh2bGlnQjhpRnRsVnR5aEVManFiQTMwcVRmWWtyeUEzdXMzQi83dDlackFBRWZyTkVoWWljMlNjMXJTYTE1emc9PTwvZHM6U2lnbmF0dXJlVmFsdWU+PEtleUluZm8geG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyMiPjxkczpYNTA5RGF0YT48ZHM6WDUwOUNlcnRpZmljYXRlPk1JSUM1akNDQWM2Z0F3SUJBZ0lRZVZnNHlLR3YyNEZPRkc2MzNtMll1VEFOQmdrcWhraUc5dzBCQVFzRkFEQXZNUzB3S3dZRFZRUURFeVJCUkVaVElGTnBaMjVwYm1jZ0xTQm5ZV3hoZUhrdVkyRndhWFJoYkdGdVpDNWpiMjB3SGhjTk1qRXdNVEl3TVRNek56TXpXaGNOTXpFd01URTRNVE16TnpNeldqQXZNUzB3S3dZRFZRUURFeVJCUkVaVElGTnBaMjVwYm1jZ0xTQm5ZV3hoZUhrdVkyRndhWFJoYkdGdVpDNWpiMjB3Z2dFaU1BMEdDU3FHU0liM0RRRUJBUVVBQTRJQkR3QXdnZ0VLQW9JQkFRRGdkZGZLUmpTY21mYnphU3dBOVdVcXFaak4wZVJ3eUFMb2wzeHNQTEZXRUxFWjBPSmw5dE5OQ1YwV3FwYWNiSjZSQlZ0azQ0RllRY2FNVWx5N25ScUE4eW11TG53eWxRZTR6VW1JQ0NybTNtVnZudVE2R29KYXlqVEFvWE5qaWpRaUtYREZjSGZ5TlFJcGJxNW1zMW1LU1pIV2hDMzZKVnRJaDRyZEFmcEkvRGUyNkNUd1lCenIzZWJqQmh2WDdsUnJUN2sxNEkzcExCU1I4a0Vwa3hON2ZseEk2RVFDVGk3U25wYkFMVmRJZDRPcUFIcVpiVmVkSDB3Q1BLVk5GaDZSZTRaaEJWck5hRWtvV0lQQytXYkNQR0d4bjg5aFdSYTI3eHJWQyt6Ym9hcHlUd2c5eHdxZDEwaldCWkg2WnNFVURwUDZLclZQWDRaMzIyTU4rcUdUQWdNQkFBRXdEUVlKS29aSWh2Y05BUUVMQlFBRGdnRUJBTTdxNjFIMG43Tk9BWUh2dFNRaDZYbXlyejE1MXUvTVplVTc5ZVdVOWFIem16OG5HK3FDQ3Y2SjJCa25CNndZNklyMmhROVhNU1RPSFVtZjJuVFRkSmZ1eVNmcVd1ZjFLUWtIUWVuOExtL0pYZXlFWXV4bzRMamJUcnRDdFkwUDNpTDVKZ2pPa3hwaFdjZVRwakNWVDhvbzg1Uytab1ZlcUdnNkdiOXFBdkhEZmlhS0lrQUVBcDBIcEtocDJ0VlBhRmpkR2Z1bWRCWmtuV3ZzYzM3VUpJcFFwK1ZoKzJub2ptZUpkd3o3WFZFUWdYUGozcjE5MzVTMklpdVhPUXhKNXR4RkI4SUVza3RoNFgzWXNGem9QVzYxMzB3ZTV0dEtkM0JyKzBnd1E4b1pEOEI4L1pRL2RiUkVuSFFwZUVmRG9jTVJqeTNHS3hIaUU4N2pLb3VuRG9jPTwvZHM6WDUwOUNlcnRpZmljYXRlPjwvZHM6WDUwOURhdGE+PC9LZXlJbmZvPjwvZHM6U2lnbmF0dXJlPjxTdWJqZWN0PjxTdWJqZWN0Q29uZmlybWF0aW9uIE1ldGhvZD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmNtOmJlYXJlciI+PFN1YmplY3RDb25maXJtYXRpb25EYXRhIEluUmVzcG9uc2VUbz0iX2YyZjUwZTQzLWU0YmItNDJkNy1hYTMwLWZkNjJhNDU2NWFmOSIgTm90T25PckFmdGVyPSIyMDI1LTEwLTIxVDA4OjQyOjMwLjM0NVoiIFJlY2lwaWVudD0iaHR0cHM6Ly9seW54YWkuY2FwaXRhbGFuZC5jb20uY24vdHlwZWtleS9hcGkvdjEvc3NvL3NhbWwvY2FsbGJhY2siIC8+PC9TdWJqZWN0Q29uZmlybWF0aW9uPjwvU3ViamVjdD48Q29uZGl0aW9ucyBOb3RCZWZvcmU9IjIwMjUtMTAtMjFUMDg6Mzc6MzAuMzI5WiIgTm90T25PckFmdGVyPSIyMDI1LTEwLTIxVDA5OjM3OjMwLjMyOVoiPjxBdWRpZW5jZVJlc3RyaWN0aW9uPjxBdWRpZW5jZT5odHRwczovL2x5bnhhaS5jYXBpdGFsYW5kLmNvbS5jbi9TQU1ML0xvZ2luPC9BdWRpZW5jZT48L0F1ZGllbmNlUmVzdHJpY3Rpb24+PC9Db25kaXRpb25zPjxBdHRyaWJ1dGVTdGF0ZW1lbnQ+PEF0dHJpYnV0ZSBOYW1lPSJodHRwOi8vc2NoZW1hcy54bWxzb2FwLm9yZy93cy8yMDA1LzA1L2lkZW50aXR5L2NsYWltcy9lbWFpbGFkZHJlc3MiPjxBdHRyaWJ1dGVWYWx1ZT52cl9odWEuamlheWlAY2FwaXRhbGFuZC5jb208L0F0dHJpYnV0ZVZhbHVlPjwvQXR0cmlidXRlPjwvQXR0cmlidXRlU3RhdGVtZW50PjxBdXRoblN0YXRlbWVudCBBdXRobkluc3RhbnQ9IjIwMjUtMTAtMjFUMDg6MzY6NDQuOTQ5WiI+PEF1dGhuQ29udGV4dD48QXV0aG5Db250ZXh0Q2xhc3NSZWY+dXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmFjOmNsYXNzZXM6UGFzc3dvcmRQcm90ZWN0ZWRUcmFuc3BvcnQ8L0F1dGhuQ29udGV4dENsYXNzUmVmPjwvQXV0aG5Db250ZXh0PjwvQXV0aG5TdGF0ZW1lbnQ+PC9Bc3NlcnRpb24+PC9zYW1scDpSZXNwb25zZT4=");
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
