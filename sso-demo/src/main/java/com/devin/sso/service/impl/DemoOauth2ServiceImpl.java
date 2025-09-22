package com.devin.sso.service.impl;

import com.devin.sso.common.configuration.Oauth2Config;
import com.devin.sso.common.properties.SsoProperty;
import com.devin.sso.entity.IdTokenClaims;
import com.devin.sso.service.DemoOauth2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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
@Service
@RequiredArgsConstructor
public class DemoOauth2ServiceImpl implements DemoOauth2Service {

    private final RestTemplate restTemplate;

    @Override
    public IdTokenClaims exchangeCodeForTokensOAuth2Style(String code, Oauth2Config azureConfig) {
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

        // 第一步：交换访问令牌（纯 OAuth2）
        ResponseEntity<Map> response = restTemplate.postForEntity(
                azureConfig.getTokenEndpoint(),
                requestEntity,
                Map.class
        );

        Map<String, Object> responseBody = response.getBody();
        String accessToken = (String) responseBody.get("access_token");

        // 第二步：使用访问令牌调用用户信息端点
        return getUserInfoWithAccessToken(accessToken, azureConfig);
    }

    private IdTokenClaims getUserInfoWithAccessToken(final String accessToken, Oauth2Config azureConfig) {
        // 准备请求头，携带访问令牌
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        // 调用用户信息端点（这是 OAuth2 + Resource Server 的方式）
        // 注意：需要根据实际的 Azure ADFS 用户信息端点 URL 调整
        String userInfoEndpoint = "http://localhost:14008/me"; // 或其他实际端点

        ResponseEntity<Map> response = restTemplate.exchange(
                userInfoEndpoint,
                org.springframework.http.HttpMethod.GET,
                requestEntity,
                Map.class
        );

        Map<String, Object> userInfo = response.getBody();

        // 从用户信息响应中提取数据
        return new IdTokenClaims(
                (String) userInfo.get("sub"),           // subject
                (String) userInfo.get("name"),          // name
                (String) userInfo.get("email")          // email
        );
    }
}
