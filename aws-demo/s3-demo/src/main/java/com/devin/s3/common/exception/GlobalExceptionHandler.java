package com.devin.s3.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * 2025/4/16 15:36.
 *
 * <p>
 *     全局异常处理器
 * </p>
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理验证异常.
     * @param e 验证异常
     */
    @ExceptionHandler(VerificationException.class)
    public void handleVerificationException(final VerificationException e) {
        log.error("========>>> 验证异常: {} <<<========", e.getMessage());
    }
}
