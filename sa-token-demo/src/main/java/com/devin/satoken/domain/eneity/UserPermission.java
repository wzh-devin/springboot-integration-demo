package com.devin.satoken.domain.eneity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 2025/4/22 23:26.
 *
 * <p></p>
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 */
@Data
@AllArgsConstructor
public class UserPermission {

    /**
     * 权限id.
     */
    private Long id;

    /**
     * 权限.
     */
    private String permission;
}
