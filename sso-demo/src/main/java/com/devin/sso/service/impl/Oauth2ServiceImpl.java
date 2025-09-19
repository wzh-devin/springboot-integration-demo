package com.devin.sso.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.devin.sso.common.properties.SsoProperty;
import com.devin.sso.entity.IdTokenClaims;
import com.devin.sso.entity.User;
import com.devin.sso.service.Oauth2Service;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URL;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * 2025/9/19 10:51.
 *
 * <p></p>
 *
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class Oauth2ServiceImpl implements Oauth2Service {
    
    private final RestTemplate restTemplate;

    private final SsoProperty ssoProperty;

    private final UserService userService;
    
    @Override
    public String generateGiteeLoginURL() {

        return "";
    }

    @Override
    public IdTokenClaims exchangeCodeForTokensAndVerify(final String code) {
        SsoProperty.OAuth2Property oauth2Config = ssoProperty.getOauth2();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth("410b171f1454f505bf7482e9f15d52c717b33e37f42d1d43a10c735e233b4989", "5c0a1dc6784ca0b8f3f473c8ea8a35819b6cca21c6b34fcaa0695457bf49982f");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("redirect_uri", "http://192.168.110.27:8080/sso/oauth/callback");

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                oauth2Config.getTokenUri(),
                requestEntity,
                Map.class
        );
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Failed to exchange token: " + response.getBody());
        }

        String idTokenString = (String) response.getBody().get("id_token");
        if (idTokenString == null) {
            throw new RuntimeException("ID Token not found in response.");
        }

        IdTokenClaims idTokenClaims = verifyIdToken(idTokenString);
        return idTokenClaims;
    }

    @Override
    public User login(IdTokenClaims claims) {
        String ssoUserId = claims.getSubject();
        LambdaQueryWrapper queryWrapper = new LambdaQueryWrapper<User>()
                .eq(User::getSsoId, ssoUserId)
                .last("limit 1");
        User user = userService.getOne(queryWrapper);
        if (user != null) {
            return user;
        }

        // 初始化用户
        user = new User();
        user.setSsoId(ssoUserId);
        user.setUsername(claims.getSubject());

        userService.save(user);

        return user;
    }

    @SneakyThrows
    public IdTokenClaims verifyIdToken(final String idTokenString) {
        SsoProperty.OAuth2Property oauth2Config = ssoProperty.getOauth2();
        
        // 1. 创建 JWT 处理器
        ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();

        // 2. CHANGED: 使用 JWKSourceBuilder 配置 JWK 源，用于获取验证签名的公钥
        // 这会自动处理缓存
        JWKSource<SecurityContext> keySource = JWKSourceBuilder.create(new URL(oauth2Config.getJwkSetUri())).build();

        // 3. CHANGED: 配置期望的 JWS 算法（如 RS256）和公钥源
        // 必须明确指定预期的算法，这是一个安全增强
        JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(
                Set.of(JWSAlgorithm.RS256),
                keySource
        );
        jwtProcessor.setJWSKeySelector(keySelector);

        // 5. 执行验证
        JWTClaimsSet claimsSet = jwtProcessor.process(idTokenString, null);

        // 6. 提取用户信息并返回
        return new IdTokenClaims(
                claimsSet.getSubject(),
                claimsSet.getStringClaim("name"),
                claimsSet.getStringClaim("email")
        );
    }
}
