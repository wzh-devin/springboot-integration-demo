package com.devin.satoken.domain.eneity;

import lombok.Data;
import java.util.List;

/**
 * 2025/4/22 23:26.
 *
 * <p></p>
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 */
@Data
public class UserRole {

    /**
     * 角色id.
     */
    private Long id;

    /**
     * 角色.
     */
    private String role;

    /**
     * 角色权限.
     */
    private List<UserPermission> permissions;
}
