package com.devin.satoken.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONUtil;
import com.devin.satoken.common.constant.RedisKey;
import com.devin.satoken.common.util.RedisUtil;
import com.devin.satoken.domain.LoginDto;
import com.devin.satoken.domain.eneity.User;
import com.devin.satoken.domain.eneity.UserPermission;
import com.devin.satoken.domain.eneity.UserRole;
import com.devin.satoken.service.SaTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

/**
 * 2025/4/19 16:27.
 *
 * <p></p>
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class SaTokenServiceImpl implements SaTokenService {

    private final RedisUtil redisUtil;

    @Override
    public SaTokenInfo login(final LoginDto loginDto) {
        // 创建用户
        User user = new User();
        user.setId(1L);
        user.setName("admin");
        user.setRoles(getRoles());

        // 参数校验
        // 这里只是做一步模拟操作，需要具体查询数据库
        if ("admin".equals(loginDto.getUsername()) && "123456".equals(loginDto.getPassword())) {
            StpUtil.login(user.getId());
            String userKey = RedisKey.generateRedisKey(RedisKey.LOGIN_INFO, user.getId());
            // 存储缓存
            redisUtil.set(userKey, JSONUtil.toJsonStr(user));
        }
        return StpUtil.getTokenInfo();
    }

    private List<UserPermission> getPermissions() {
        List<UserPermission> permissions = new ArrayList<>();
        permissions.add(new UserPermission(1L, "select"));
        permissions.add(new UserPermission(2L, "update"));
        permissions.add(new UserPermission(3L, "insert"));
        return permissions;
    }

    private List<UserRole> getRoles() {
        UserRole role = new UserRole();
        role.setId(1L);
        role.setRole("root");
        role.setPermissions(getPermissions());
        return List.of(role);
    }
}
