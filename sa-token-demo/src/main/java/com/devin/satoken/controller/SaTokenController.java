package com.devin.satoken.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.SaTokenInfo;
import com.devin.common.utils.ApiResult;
import com.devin.satoken.domain.LoginDto;
import com.devin.satoken.service.SaTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 2025/4/19 16:27.
 *
 * <p></p>
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/sa-token")
@RequiredArgsConstructor
@Tag(name = "Sa-Token相关接口")
public class SaTokenController {

    private final SaTokenService saTokenService;

    /**
     * 登录.
     * @param loginDto 登录信息
     * @return Token
     */
    @PostMapping("/login")
    @Operation(summary = "登录")
    @Parameter(name = "loginDto", description = "登录信息", required = true)
    public ApiResult<SaTokenInfo> login(@RequestBody final LoginDto loginDto) {
        return ApiResult.success(saTokenService.login(loginDto));
    }

    /**
     * 测试权限.
     * <b>这个会失败</b>
     * @return success
     */
    @GetMapping("/testPermission")
    @SaCheckPermission("add")
    @Operation(summary = "测试权限")
    public ApiResult<String> testPermission() {
        return ApiResult.success("success");
    }

    /**
     * 测试角色.
     * @return success
     */
    @GetMapping("/testRole")
    @SaCheckRole("root")
    @Operation(summary = "测试角色")
    public ApiResult<String> testRole() {
        return ApiResult.success("success");
    }
}
