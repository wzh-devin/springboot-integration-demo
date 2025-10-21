package com.devin.sso.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.devin.sso.common.configuration.Oauth2Config;
import com.devin.sso.common.properties.SsoProperty;
import com.devin.sso.entity.IdTokenClaims;
import com.devin.sso.entity.User;
import com.devin.sso.service.DemoOauth2Service;
import com.devin.sso.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 2025/9/22 12:32.
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
public class DemoOauth2ServiceImpl implements DemoOauth2Service {

    private final RestTemplate restTemplate;

    private final UserService userService;

    @Override
    public IdTokenClaims exchangeCodeForTokensOAuth2Style(final String code, final Oauth2Config azureConfig) {
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
        body.add("scope", "profile email");

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        // 交换访问令牌
        ResponseEntity<Map> response = restTemplate.postForEntity(
                azureConfig.getTokenEndpoint(),
                requestEntity,
                Map.class
        );

        Map<String, Object> responseBody = response.getBody();
        String accessToken = (String) responseBody.get("access_token");

        // 使用访问令牌调用用户信息端点
        return getUserInfoWithAccessToken(accessToken, azureConfig);
    }

    @Override
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

    private IdTokenClaims getUserInfoWithAccessToken(final String accessToken, final Oauth2Config azureConfig) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

//        String userInfoEndpoint = "http://localhost:14008/api/userinfo";
        String userInfoEndpoint = "https://graph.microsoft.com/v1.0/me";

        ResponseEntity<Map> response = restTemplate.exchange(
                userInfoEndpoint,
                org.springframework.http.HttpMethod.GET,
                requestEntity,
                Map.class
        );

        Map<String, Object> userInfo = response.getBody();

        // 从用户信息响应中提取数据
        String subject = (String) userInfo.get("id");

        String name = (String) userInfo.get("displayName");

        String email = (String) userInfo.get("mail");

        log.info("Extracted user info - subject: {}, displayName: {}, mail: {}", subject, name, email);

        return new IdTokenClaims(subject, name, email);
    }
}
