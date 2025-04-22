package com.devin.satoken.domain.eneity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.List;

/**
 * 2025/4/21 14:51.
 *
 * <p></p>
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable {

    /**
     * 用户id.
     */
    private Long id;

    /**
     * 用户名.
     */
    private String name;

    /**
     * 用户角色.
     */
    private List<UserRole> roles;
}
