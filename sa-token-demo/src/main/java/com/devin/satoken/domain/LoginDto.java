package com.devin.satoken.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
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

    @SchemaProperty(name = "账号")
    private String username;

    @SchemaProperty(name = "密码")
    private String password;
}
