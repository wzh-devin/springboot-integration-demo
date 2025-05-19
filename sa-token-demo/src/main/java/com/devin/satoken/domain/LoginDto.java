package com.devin.satoken.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 2025/4/19 16:47.
 *
 * <p></p>
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 */
@Data
@Schema(description = "登录")
public class LoginDto {

    @Schema(description = "账号")
    private String username;

    @Schema(description = "密码")
    private String password;
}
