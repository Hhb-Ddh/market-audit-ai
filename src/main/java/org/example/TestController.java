package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@RestController
public class TestController {

    @Autowired
    private JedisPool jedisPool;

    @GetMapping("/test")
    public String testRedis() {
        // 从池子里借一个连接，用完自动还回去
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.set("my_test_key", "Hello Spring Boot!");
            String value = jedis.get("my_test_key");
            return "从 Redis 查到的结果是：" + value;
        }
    }
}