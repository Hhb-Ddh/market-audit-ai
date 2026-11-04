package com.ddh.test;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.util.Map;

public class jedisTest {
    private Jedis jedis;

    @Before
    public void setUp() throws Exception {
        jedis=new Jedis("192.168.152.128",6379);
        jedis.auth("123456");
        jedis.select(2);
    }

    @Test
    public void TestHash() {
        jedis.hset("users:user1","name","唐老鸭");
        jedis.hset("users:user1","age","27");
        Map<String, String> stringStringMap = jedis.hgetAll("users:user1");
    }

    @After
    public void tearDown() throws Exception {
        if(jedis !=null){
            jedis.close();
        }

    }
}
