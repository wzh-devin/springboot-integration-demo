package com.devin.satoken.service;

import cn.dev33.satoken.stp.SaTokenInfo;
import com.devin.satoken.domain.LoginDto;

/**
 * 2025/4/19 16:27.
 *
 * <p></p>
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 */
public interface SaTokenService {

    /**
     * 登录.
     * @param loginDto 登录信息
     * @return Token
     */
    SaTokenInfo login(LoginDto loginDto);
}
