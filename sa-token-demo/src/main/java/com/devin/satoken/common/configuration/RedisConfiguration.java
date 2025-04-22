package com.devin.satoken.common.configuration;

/**
 * 2025/4/22 20:42.
 *
 * <p>
 *     redis配置类，可做参考
 *     目前使用的是RedisUtil {@linkplain com.devin.satoken.common.util.RedisUtil}工具，采取 StringRedisTemplate 实现
 *     因此暂不需要配置序列化
 * </p>
 * @author <a href="https://github.com/wzh-devin">devin</a>
 * @version 1.0
 * @since 1.0
 * @deprecated 暂不需要配置序列化器
 */
@Deprecated
//@Configuration
public class RedisConfiguration {
//    /**
//     * 配置自定义序列化器.
//     * @param redisConnectionFactory redis连接工厂
//     * @return RedisTemplate
//     */
//    @Bean
//    public RedisTemplate<Object, Object> redisTemplate(final RedisConnectionFactory redisConnectionFactory) {
//        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
//        redisTemplate.setConnectionFactory(redisConnectionFactory);
//        // 使用 GenericJackson2JsonRedisSerializer 替换默认序列化
//        GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer = new GenericJackson2JsonRedisSerializer();
//        // 设置key和value的序列化规则
//        redisTemplate.setKeySerializer(new GenericToStringSerializer<>(Object.class));
//        redisTemplate.setValueSerializer(genericJackson2JsonRedisSerializer);
//        // 设置hashKey和hashValue的序列化规则
//        redisTemplate.setHashKeySerializer(new GenericToStringSerializer<>(Object.class));
//        redisTemplate.setHashValueSerializer(genericJackson2JsonRedisSerializer);
//        // 设置支持事物
//        redisTemplate.setEnableTransactionSupport(true);
//        redisTemplate.afterPropertiesSet();
//        return redisTemplate;
//    }
}
