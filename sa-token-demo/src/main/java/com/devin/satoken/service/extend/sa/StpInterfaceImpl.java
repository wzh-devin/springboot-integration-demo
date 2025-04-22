package com.devin.satoken.service.extend.sa;

import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.json.JSONUtil;
import com.devin.satoken.common.constant.RedisKey;
import com.devin.satoken.common.util.RedisUtil;
import com.devin.satoken.domain.eneity.User;
import com.devin.satoken.domain.eneity.UserPermission;
import com.devin.satoken.domain.eneity.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

/**
 * 2025/4/19 21:26.
 *
 * <p>
 *     配置sa-token的权限缓存策略<br>
 *     这里只做简单演示，具体操作，需要根据业务进行修改
 * </p>
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final RedisUtil redisUtil;

    @Override
    public List<String> getPermissionList(final Object loginId, final String loginType) {
        // 1. 声明权限码集合
        List<String> list = new ArrayList<>();

        // 2. 遍历角色列表，查询拥有的权限码
        List<UserRole> roles = JSONUtil.toBean(redisUtil.get(RedisKey.generateRedisKey(RedisKey.LOGIN_INFO, loginId)), User.class).getRoles();

        roles.forEach(role -> {
            list.addAll(role.getPermissions().stream()
                    .map(UserPermission::getPermission)
                    .toList());
        });

        // 3. 返回权限码集合
        return list;
    }

    @Override
    public List<String> getRoleList(final Object loginId, final String loginType) {
        User user = JSONUtil.toBean(redisUtil.get(RedisKey.generateRedisKey(RedisKey.LOGIN_INFO, loginId)), User.class);
        // 获取角色信息集合
        List<String> role = user.getRoles()
                .stream()
                .map(UserRole::getRole)
                .toList();
        return role;
    }
}
