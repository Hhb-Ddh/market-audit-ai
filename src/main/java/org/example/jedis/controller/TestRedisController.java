package org.example.jedis.controller;

import org.example.jedis.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestRedisController {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // ✅ 新代码注解说明：@Qualifier 用于指定注入 Bean 的名称，避免因存在多个同类型 RedisTemplate 导致注入歧义
    @Autowired
    @Qualifier("yudaoRedisTemplate")
    private RedisTemplate<String, Object> yudaoRedisTemplate;

    @GetMapping("/test-compare")
    public String testCompare() {
        User user = new User("李四", "30");

        // ❌ 旧代码：仅使用旧序列化器
        // redisTemplate.opsForValue().set("user:compare:old", user);

        // ✅ 新代码：分别用旧和新序列化器存入，方便去 Redis 客户端对比时间格式

        yudaoRedisTemplate.opsForValue().set("user:compare:new", user);

        return "已分别存入！请去 Redis 客户端对比 user:compare:old 和 user:compare:new 的时间格式。";
    }
}