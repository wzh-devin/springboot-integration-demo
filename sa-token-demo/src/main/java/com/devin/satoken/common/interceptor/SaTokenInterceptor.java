package com.devin.satoken.common.interceptor;

import cn.dev33.satoken.exception.BackResultException;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.StopMatchException;
import cn.dev33.satoken.fun.SaParamFunction;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.strategy.SaAnnotationStrategy;
import cn.hutool.json.JSONUtil;
import com.devin.satoken.common.constant.RedisKey;
import com.devin.satoken.common.util.RedisUtil;
import com.devin.satoken.domain.eneity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * 2025/4/19 18:05.
 *
 * <p>
 *     对SaInterceptor进行更细化的重写<br>
 *     这里只是对拦截的路径新增了一个日志打印，如果有更精细化操作，也可以自定义重写
 * </p>
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@Component
public class SaTokenInterceptor extends SaInterceptor {

    @Autowired
    private RedisUtil redisUtil;

    public SaTokenInterceptor() {
        // 返回自定义的策略
        super.auth = handler -> {
            // 判断是否登录
            StpUtil.checkLogin();

            // 从Redis中获取数据
            Object userId = StpUtil.getLoginIdDefaultNull();
            String userKey = RedisKey.generateRedisKey(RedisKey.LOGIN_INFO, userId);
            User user = JSONUtil.toBean(redisUtil.get(userKey), User.class);

            if (Objects.isNull(user)) {
                throw new NotLoginException("登录已过期", null, null);
            }
        };
    }

    public SaTokenInterceptor(final SaParamFunction<Object> auth) {
        super.auth = auth;
    }

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) throws Exception {
        try {
            log.info("============ 拦截的请求路径为：{} ============", request.getRequestURI());
            if (this.isAnnotation && handler instanceof HandlerMethod) {
                Method method = ((HandlerMethod) handler).getMethod();
                SaAnnotationStrategy.instance.checkMethodAnnotation.accept(method);
            }

            this.auth.run(handler);
        } catch (StopMatchException ignored) {
        } catch (BackResultException e) {
            if (response.getContentType() == null) {
                response.setContentType("text/plain; charset=utf-8");
            }

            response.getWriter().print(e.getMessage());
            return false;
        }

        return true;
    }
}
