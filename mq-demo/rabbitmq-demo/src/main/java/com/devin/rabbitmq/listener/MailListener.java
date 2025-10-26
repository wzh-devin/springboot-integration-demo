package com.devin.rabbitmq.listener;

import com.devin.rabbitmq.common.constant.RabbitMqConstant;
import com.devin.rabbitmq.domain.vo.MailVO;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * 2025/10/27 00:17.
 *
 * <p></p>
 *
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MailListener {

    private static final Integer MAX_RETRY_COUNT = 3;

    private final JavaMailSender mailSender;

    private final RabbitTemplate rabbitTemplate;

    @Value("${spring.mail.username}")
    private String from;

    /**
     * 监听邮件队列.
     *
     * @param mailVO      邮件对象
     * @param channel     通道
     * @param deliveryTag 消息的标识
     * @param retryCount  重试次数
     */
    @RabbitListener(
            queues = RabbitMqConstant.MAIL_QUEUE,
            ackMode = "MANUAL"
    )
    public void sendMail(
            final MailVO mailVO,
            final Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) final long deliveryTag,
            @Header(name = "x-retry-count", required = false) final Integer retryCount) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(mailVO.getTo());
            mailMessage.setFrom(from);
            mailMessage.setSubject(mailVO.getSubject());
            mailMessage.setText(mailVO.getContent());
            mailSender.send(mailMessage);

            // 发送成功，确认消息
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            // 执行失败逻辑
            handleFailMessage(mailVO, channel, deliveryTag, retryCount == null ? 0 : retryCount, e.getMessage());
        }
    }

    /**
     * 处理失败消息.
     *
     * @param mailVO      邮件对象
     * @param channel     通道
     * @param deliveryTag 消息的标识
     * @param retryCount  重试次数
     * @param errMessage  错误信息
     */
    public void handleFailMessage(
            final MailVO mailVO,
            final Channel channel,
            final long deliveryTag,
            final Integer retryCount,
            final String errMessage
    ) {
        try {
            if (retryCount < MAX_RETRY_COUNT) {
                // 进行消息重试
                rabbitTemplate.convertAndSend(
                        RabbitMqConstant.MAIL_RETRY_EXCHANGE,
                        RabbitMqConstant.MAIL_RETRY_ROUTING_KEY,
                        mailVO,
                        msg -> {
                            msg.getMessageProperties().setHeader("x-retry-count", retryCount + 1);
                            return msg;
                        }
                );

                // 确认原消息
                channel.basicAck(deliveryTag, false);
            } else {
                // 最大重试后，消息放入死信队列
                rabbitTemplate.convertAndSend(
                        RabbitMqConstant.MAIL_DLX_EXCHANGE,
                        RabbitMqConstant.MAIL_DLX_ROUTING_KEY,
                        mailVO,
                        msg -> {
                            msg.getMessageProperties().setHeader("x-retry-count", retryCount);
                            msg.getMessageProperties().setHeader("x-failure-reason", errMessage);
                            msg.getMessageProperties().setHeader("x-failure-time", LocalDateTime.now().toString());
                            return msg;
                        }
                );

                // 确认原消息
                channel.basicAck(deliveryTag, false);
            }
        } catch (IOException e) {
            log.error("处理失败消息失败: {}", e.getMessage());
        }
    }
}
