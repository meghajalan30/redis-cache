package com.example.cache.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.example.cache.model.Product;


@EnableCaching
@Configuration
public class CachingConfiguration {


    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
       RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration("localhost", 6379);
       return new JedisConnectionFactory(redisStandaloneConfiguration);
    }
    
    
    @Bean
    RedisTemplate<String, Product> redisTemplate() {
        RedisTemplate<String, Product> redisTemplate = new RedisTemplate<String, Product>();
        redisTemplate.setConnectionFactory(jedisConnectionFactory());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        //redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        return redisTemplate;
     }
    
}