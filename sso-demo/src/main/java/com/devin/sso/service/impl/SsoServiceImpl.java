package com.devin.sso.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.devin.sso.common.properties.SsoProperty;
import com.devin.sso.entity.IdTokenClaims;
import com.devin.sso.entity.User;
import com.devin.sso.service.SsoService;
import com.devin.sso.service.UserService;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URL;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.Set;

/**
 * 2025/9/17 21:23.
 *
 * <p></p>
 *
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SsoServiceImpl implements SsoService {

    private final SsoProperty ssoProperty;
    private final RestTemplate restTemplate;
    private final UserService userService;

    /**
     * 生成 Azure ADFS 登录 URL.
     *
     * @return Azure ADFS 登录 URL
     */
    public String generateAzureAdfsLoginUrl() {
        SsoProperty.AzureAdfsProperty azureConfig = ssoProperty.getAzureAdfs();
        
        // 构建授权 URL
        return UriComponentsBuilder
                .fromUriString(azureConfig.getAuthorizationEndpoint())
                .queryParam("client_id", azureConfig.getClientId())
                .queryParam("response_type", "code")
                .queryParam("redirect_uri", azureConfig.getRedirectUri())
                .queryParam("scope", azureConfig.getScope())
                .queryParam("state", generateState())
                .queryParam("response_mode", "query")
                .build()
                .toUriString();
    }

    /**
     * 使用授权码交换访问令牌并验证 ID 令牌.
     *
     * @param code 授权码
     * @return ID 令牌声明
     */
    public IdTokenClaims exchangeCodeForTokensAndVerify(String code) {
        SsoProperty.AzureAdfsProperty azureConfig = ssoProperty.getAzureAdfs();
        
        // 准备请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        // 准备请求体
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", azureConfig.getClientId());
        body.add("client_secret", azureConfig.getClientSecret());
        body.add("code", code);
        body.add("redirect_uri", azureConfig.getRedirectUri());
        body.add("scope", azureConfig.getScope());
        
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
        
        try {
            // 发送令牌交换请求
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    azureConfig.getTokenEndpoint(),
                    requestEntity,
                    Map.class
            );
            
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.error("Failed to exchange token: {}", response.getBody());
                throw new RuntimeException("Failed to exchange token: " + response.getBody());
            }
            
            Map<String, Object> responseBody = response.getBody();
            String accessToken = (String) responseBody.get("access_token");
            String idTokenString = (String) responseBody.get("id_token");
            
            if (idTokenString == null) {
                log.error("ID Token not found in response");
                throw new RuntimeException("ID Token not found in response.");
            }
            
            log.info("Successfully received tokens from Azure ADFS");
            
            // 验证并解析 ID 令牌
            return verifyIdToken(idTokenString);
            
        } catch (Exception e) {
            log.error("Error during token exchange: {}", e.getMessage(), e);
            throw new RuntimeException("Token exchange failed: " + e.getMessage(), e);
        }
    }

    /**
     * 验证 ID 令牌.
     *
     * @param idTokenString ID 令牌字符串
     * @return ID 令牌声明
     */
    @SneakyThrows
    public IdTokenClaims verifyIdToken(String idTokenString) {
        SsoProperty.AzureAdfsProperty azureConfig = ssoProperty.getAzureAdfs();
        
        try {
            // 创建 JWT 处理器
            ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
            
            // 配置 JWK 源，用于获取验证签名的公钥
            JWKSource<SecurityContext> keySource = JWKSourceBuilder
                    .create(new URL(azureConfig.getJwkSetUri()))
                    .build();
            
            // 配置期望的 JWS 算法和公钥源
            JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(
                    Set.of(JWSAlgorithm.RS256),
                    keySource
            );
            jwtProcessor.setJWSKeySelector(keySelector);
            
            // 执行验证
            JWTClaimsSet claimsSet = jwtProcessor.process(idTokenString, null);
            
            // 验证发行者
            String issuer = claimsSet.getIssuer();
            if (!azureConfig.getIssuer().equals(issuer)) {
                throw new RuntimeException("Invalid issuer: " + issuer);
            }
            
            // 验证受众
            if (!claimsSet.getAudience().contains(azureConfig.getClientId())) {
                throw new RuntimeException("Invalid audience");
            }
            
            log.info("ID Token validation successful for user: {}", claimsSet.getSubject());
            
            // 提取用户信息并返回
            return new IdTokenClaims(
                    claimsSet.getSubject(),
                    claimsSet.getStringClaim("name"),
                    claimsSet.getStringClaim("email")
            );
            
        } catch (Exception e) {
            log.error("ID Token validation failed: {}", e.getMessage(), e);
            throw new RuntimeException("ID Token validation failed: " + e.getMessage(), e);
        }
    }

    /**
     * 基于 ID 令牌声明进行用户登录.
     *
     * @param claims ID 令牌声明
     * @return 用户对象
     */
    public User login(IdTokenClaims claims) {
        String ssoUserId = claims.getSubject();
        
        // 查找现有用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>()
                .eq(User::getSsoId, ssoUserId)
                .last("limit 1");
        User user = userService.getOne(queryWrapper);
        
        if (user != null) {
            log.info("Existing user logged in: {}", user.getUsername());
            return user;
        }
        
        // 创建新用户
        user = new User();
        user.setSsoId(ssoUserId);
        user.setUsername(claims.getName() != null ? claims.getName() : claims.getSubject());
        
        userService.save(user);
        log.info("New user created and logged in: {}", user.getUsername());
        
        return user;
    }

    /**
     * 初始化 Azure ADFS 端点配置.
     * 通过 OAuth2 发现端点自动获取各种端点 URL.
     */
    public void initializeAzureAdfsEndpoints() {
        SsoProperty.AzureAdfsProperty azureConfig = ssoProperty.getAzureAdfs();
        
        if (azureConfig.getAuthority() == null) {
            throw new RuntimeException("Azure ADFS authority URL is not configured");
        }
        
        // 构建发现端点 URL
        String discoveryUrl = azureConfig.getAuthority().endsWith("/") 
                ? azureConfig.getAuthority() + ".well-known/openid-configuration"
                : azureConfig.getAuthority() + "/.well-known/openid-configuration";
        
        try {
            log.info("Fetching Azure ADFS configuration from: {}", discoveryUrl);
            
            // 获取 OAuth2 配置
            Map<String, Object> config = restTemplate.getForObject(discoveryUrl, Map.class);
            
            if (config == null) {
                throw new RuntimeException("Failed to fetch Azure ADFS configuration");
            }
            
            // 更新配置
            azureConfig.setIssuer((String) config.get("issuer"));
            azureConfig.setAuthorizationEndpoint((String) config.get("authorization_endpoint"));
            azureConfig.setTokenEndpoint((String) config.get("token_endpoint"));
            azureConfig.setJwkSetUri((String) config.get("jwks_uri"));
            
            log.info("Azure ADFS endpoints initialized successfully");
            log.debug("Issuer: {}", azureConfig.getIssuer());
            log.debug("Authorization Endpoint: {}", azureConfig.getAuthorizationEndpoint());
            log.debug("Token Endpoint: {}", azureConfig.getTokenEndpoint());
            log.debug("JWK Set URI: {}", azureConfig.getJwkSetUri());
            
        } catch (Exception e) {
            log.error("Failed to initialize Azure ADFS endpoints: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize Azure ADFS endpoints: " + e.getMessage(), e);
        }
    }

    /**
     * 纯 OAuth2 流程：使用授权码交换访问令牌，然后调用用户信息端点获取用户信息.
     * 这与 OIDC 流程的区别是：
     * 1. 不使用 openid scope
     * 2. 不获取 id_token
     * 3. 使用 access_token 调用 /userinfo 端点获取用户信息
     *
     * @param code 授权码
     * @return 用户信息
     */
    public IdTokenClaims exchangeCodeForTokensOAuth2Style(String code) {
        SsoProperty.AzureAdfsProperty azureConfig = ssoProperty.getAzureAdfs();
        
        // 准备请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        // 准备请求体 - 注意这里的 scope 不包含 openid
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", azureConfig.getClientId());
        body.add("client_secret", azureConfig.getClientSecret());
        body.add("code", code);
        body.add("redirect_uri", azureConfig.getRedirectUri());
        body.add("scope", "profile email"); // 注意：不包含 openid
        
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
        
        try {
            // 第一步：交换访问令牌（纯 OAuth2）
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    azureConfig.getTokenEndpoint(),
                    requestEntity,
                    Map.class
            );
            
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.error("Failed to exchange token: {}", response.getBody());
                throw new RuntimeException("Failed to exchange token: " + response.getBody());
            }
            
            Map<String, Object> responseBody = response.getBody();
            String accessToken = (String) responseBody.get("access_token");
            
            if (accessToken == null) {
                log.error("Access Token not found in response");
                throw new RuntimeException("Access Token not found in response.");
            }
            
            // 注意：纯 OAuth2 流程不会返回 id_token
            String idTokenString = (String) responseBody.get("id_token");
            if (idTokenString != null) {
                log.info("ID Token found - this indicates OIDC flow, not pure OAuth2");
            }
            
            log.info("Successfully received access token from Azure ADFS (OAuth2 style)");
            
            // 第二步：使用访问令牌调用用户信息端点
            return getUserInfoWithAccessToken(accessToken);
            
        } catch (Exception e) {
            log.error("Error during OAuth2 token exchange: {}", e.getMessage(), e);
            throw new RuntimeException("OAuth2 token exchange failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * 使用访问令牌从用户信息端点获取用户信息（纯 OAuth2 方式）.
     *
     * @param accessToken 访问令牌
     * @return 用户信息
     */
    private IdTokenClaims getUserInfoWithAccessToken(final String accessToken) {
        SsoProperty.AzureAdfsProperty azureConfig = ssoProperty.getAzureAdfs();
        
        try {
            // 准备请求头，携带访问令牌
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);
            
            // 调用用户信息端点（这是 OAuth2 + Resource Server 的方式）
            // 注意：需要根据实际的 Azure ADFS 用户信息端点 URL 调整
            String userInfoEndpoint = azureConfig.getAuthority() + "/userinfo"; // 或其他实际端点
            
            ResponseEntity<Map> response = restTemplate.exchange(
                    userInfoEndpoint,
                    org.springframework.http.HttpMethod.GET,
                    requestEntity,
                    Map.class
            );
            
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.error("Failed to get user info: {}", response.getBody());
                throw new RuntimeException("Failed to get user info: " + response.getBody());
            }
            
            Map<String, Object> userInfo = response.getBody();
            log.info("Successfully retrieved user info via OAuth2 access token");
            
            // 从用户信息响应中提取数据
            return new IdTokenClaims(
                    (String) userInfo.get("sub"),           // subject
                    (String) userInfo.get("name"),          // name
                    (String) userInfo.get("email")          // email
            );
            
        } catch (Exception e) {
            log.error("Failed to get user info with access token: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get user info: " + e.getMessage(), e);
        }
    }
    
    /**
     * 生成纯 OAuth2 登录 URL（不包含 openid scope）.
     *
     * @return OAuth2 登录 URL
     */
    public String generateOAuth2LoginUrl() {
        SsoProperty.AzureAdfsProperty azureConfig = ssoProperty.getAzureAdfs();
        
        // 构建授权 URL - 注意 scope 不包含 openid
        return UriComponentsBuilder
                .fromUriString(azureConfig.getAuthorizationEndpoint())
                .queryParam("client_id", azureConfig.getClientId())
                .queryParam("response_type", "code")
                .queryParam("redirect_uri", azureConfig.getRedirectUri())
                .queryParam("scope", "profile email") // 纯 OAuth2: 不包含 openid
                .queryParam("state", generateState())
                .queryParam("response_mode", "query")
                .build()
                .toUriString();
    }

    /**
     * 生成随机状态参数.
     *
     * @return Base64 编码的随机状态字符串
     */
    private String generateState() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
