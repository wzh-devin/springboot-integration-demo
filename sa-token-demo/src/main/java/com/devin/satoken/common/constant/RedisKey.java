package com.devin.satoken.common.constant;

/**
 * 2025/4/19 21:51.
 *
 * <p>
 *     RedisKey常量配置
 * </p>
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 */
public class RedisKey {
    /**
     * 用户登录信息.
     */
    public static final String LOGIN_INFO = "userToken:userId_%s";

    public static final String USER_PERMISSION = "userPermission:userId_%s";

    private static final String BASE_KEY = "sa-token:";

    /**
     * 生成RedisKey.
     * @param key RedisKey
     * @param args 填写的参数
     * @return RedisKey
     */
    public static String generateRedisKey(final String key, final Object... args) {
        return String.format(BASE_KEY.concat(key), args);
    }
}
