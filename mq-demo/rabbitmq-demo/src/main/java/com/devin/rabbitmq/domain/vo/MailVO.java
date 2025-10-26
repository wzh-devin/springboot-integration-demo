package com.devin.rabbitmq.domain.vo;

import lombok.Data;

/**
 * 2025/10/27 00:13.
 *
 * <p></p>
 *
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
public class MailVO {

    /**
     * 接收者.
     */
    private String to;

    /**
     * 邮件主题.
     */
    private String subject;

    /**
     * 邮件内容.
     */
    private String content;
}
