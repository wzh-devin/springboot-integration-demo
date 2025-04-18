package com.devin.s3.common.exception;

/**
 * 2025/4/16 15:37.
 *
 * <p>
 *     验证异常
 * </p>
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 */
public class VerificationException extends RuntimeException {
    public VerificationException(final String message) {
        super(message);
    }
}
