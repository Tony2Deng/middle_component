package org.example;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootApplication
public class Main {
    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate stringRedisTemplate;

    @Resource(name = "redisTemplate")
    private RedisTemplate redisTemplate;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @PostConstruct
    public void test() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        stringRedisTemplate.opsForValue().set("test", "tony");
        String s = stringRedisTemplate.opsForValue().get("test");
        System.out.println(s);
        stringRedisTemplate.delete("test");

        stringRedisTemplate.opsForList().leftPush("msg:queue", "消息1");
        stringRedisTemplate.opsForList().leftPush("msg:queue", "消息2");
        s = stringRedisTemplate.opsForList().rightPop("msg:queue");
        System.out.println("list pop" + s);
        s =stringRedisTemplate.opsForList().rightPop("msg:queue");
        System.out.println("list pop" + s);
        stringRedisTemplate.delete("msg:queue");
    }
}