package org.example.jedis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class JedisConfig {

    @Bean
    public JedisPool jedisPool() {
        // 配置连接池
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(10);

        // 如果你虚拟机有密码，改成 new JedisPool(config, "192.168.152.128", 6379, 2000, "123456")
        // 没密码就用下面这行
        return new JedisPool(config, "192.168.152.128", 6379, 2000,"123456");
    }
}