package com.devin.rabbitmq.common.constant;

/**
 * 2025/10/25 23:32.
 *
 * <p>
 *     RabbitMq常量
 * </p>
 *
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0.0
 * @since 1.0.0
 */
public class RabbitMqConstant {

    /**
     * 邮箱交换机.
     */
    public static final String MAIL_EXCHANGE = "mail.exchange";

    /**
     * 邮箱队列.
     */
    public static final String MAIL_QUEUE = "mail.queue";

    /**
     * 邮箱路由.
     */
    public static final String MAIL_ROUTING_KEY = "mail.send";

    /**
     * 邮箱死信交换机.
     */
    public static final String MAIL_DLX_EXCHANGE = "mail.dlx.exchange";

    /**
     * 邮箱死信队列.
     */
    public static final String MAIL_DLX_QUEUE = "mail.dlx.queue";

    /**
     * 邮箱死信路由.
     */
    public static final String MAIL_DLX_ROUTING_KEY = "mail.dlx";

    /**
     * 邮箱重试交换机.
     */
    public static final String MAIL_RETRY_EXCHANGE = "mail.retry.exchange";

    /**
     * 邮箱重试队列.
     */
    public static final String MAIL_RETRY_QUEUE = "mail.retry.queue";

    /**
     * 邮箱重试路由.
     */
    public static final String MAIL_RETRY_ROUTING_KEY = "mail.retry";
}
