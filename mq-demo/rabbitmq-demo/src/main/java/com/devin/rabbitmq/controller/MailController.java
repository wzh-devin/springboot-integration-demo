package com.devin.rabbitmq.controller;

import com.devin.common.utils.ApiResult;
import com.devin.rabbitmq.common.constant.RabbitMqConstant;
import com.devin.rabbitmq.domain.vo.MailVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 2025/10/27 00:04.
 *
 * <p></p>
 *
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@Tag(name = "邮件服务")
@RestController
@RequestMapping("/mail")
@RequiredArgsConstructor
public class MailController {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送邮件.
     * @param mail 邮件对象
     * @return 响应结果
     */
    @PostMapping("/send")
    public ApiResult<Void> send(
            @RequestBody final MailVO mail
    ) {
        log.info("开始发送邮件");
        rabbitTemplate.convertAndSend(
                RabbitMqConstant.MAIL_EXCHANGE,
                RabbitMqConstant.MAIL_ROUTING_KEY,
                mail,
                msg -> {
                    msg.getMessageProperties().setHeader("x-retry-count", 0);
                    msg.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    return msg;
                });
        return ApiResult.success();
    }
}
