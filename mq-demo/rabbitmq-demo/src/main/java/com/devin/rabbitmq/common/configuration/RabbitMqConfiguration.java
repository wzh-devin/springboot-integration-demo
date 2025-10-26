package com.devin.rabbitmq.common.configuration;

import com.devin.rabbitmq.common.constant.RabbitMqConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 2025/10/25 21:03.
 *
 * <p>
 * rabbitmq相关配置
 * </p>
 *
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0.0
 * @since 1.0.0
 */
@Configuration
public class RabbitMqConfiguration {

    /**
     * 邮箱队列.
     *
     * @return Queue
     */
    @Bean
    public Queue mailQueue() {
        return QueueBuilder.durable(RabbitMqConstant.MAIL_QUEUE)
                .maxLength(10000L)
                .build();
    }

    /**
     * 邮箱交换机.
     *
     * @return TopicExchange
     */
    @Bean
    public TopicExchange mailExchange() {
        return new TopicExchange(RabbitMqConstant.MAIL_EXCHANGE, true, false);
    }

    /**
     * 邮箱重试队列.
     *
     * @return Queue
     */
    @Bean
    public Queue mailRetryQueue() {
        return QueueBuilder.durable(RabbitMqConstant.MAIL_RETRY_QUEUE)
                .ttl(5000)
                .deadLetterExchange(RabbitMqConstant.MAIL_EXCHANGE)
                .deadLetterRoutingKey(RabbitMqConstant.MAIL_ROUTING_KEY)
                .build();
    }

    /**
     * 邮箱重试交换机.
     *
     * @return TopicExchange
     */
    @Bean
    public TopicExchange mailRetryExchange() {
        return new TopicExchange(RabbitMqConstant.MAIL_RETRY_EXCHANGE, true, false);
    }

    /**
     * 邮箱死信队列.
     *
     * @return Queue
     */
    @Bean
    public Queue mailDlxQueue() {
        return QueueBuilder.durable(RabbitMqConstant.MAIL_DLX_QUEUE)
                .build();
    }

    /**
     * 邮箱死信交换机.
     *
     * @return TopicExchange
     */
    @Bean
    public TopicExchange mailDlxExchange() {
        return new TopicExchange(RabbitMqConstant.MAIL_DLX_EXCHANGE, true, false);
    }

    /**
     * 绑定邮箱队列.
     *
     * @return Binding
     */
    @Bean
    public Binding mailBinding() {
        return BindingBuilder.bind(mailQueue())
                .to(mailExchange())
                .with(RabbitMqConstant.MAIL_ROUTING_KEY);
    }

    /**
     * 绑定邮箱重试队列.
     *
     * @return Binding
     */
    @Bean
    public Binding mailRetryBinding() {
        return BindingBuilder.bind(mailRetryQueue())
                .to(mailRetryExchange())
                .with(RabbitMqConstant.MAIL_RETRY_ROUTING_KEY);
    }

    /**
     * 绑定邮箱死信队列.
     *
     * @return Binding
     */
    @Bean
    public Binding mailDlxBinding() {
        return BindingBuilder.bind(mailDlxQueue())
                .to(mailDlxExchange())
                .with(RabbitMqConstant.MAIL_DLX_ROUTING_KEY);
    }
}
