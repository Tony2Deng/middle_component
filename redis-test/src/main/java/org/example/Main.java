package org.example;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.example.service.RedisDistributedLockService;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
public class Main {
    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate stringRedisTemplate;

    @Resource(name = "redisTemplate")
    private RedisTemplate redisTemplate;

    @Resource
    private RedisDistributedLockService redisDistributedLockService;

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

        HashMap<String, String> stringStringHashMap = new HashMap<>();
        stringStringHashMap.put("key1", "value1");
        stringStringHashMap.put("key2", "value2");
        redisTemplate.opsForValue().set("name", stringStringHashMap);
        Object name = redisTemplate.opsForValue().get("name");
        System.out.println(name);
        stringRedisTemplate.delete("name");

        // Multi-thread demo for distributed lock
        String lockKey = "demo:lock";
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 5; i++) {
            int index = i;
            executorService.submit(() -> {
                String ownerId = "thread-" + index + "-" + UUID.randomUUID();
                System.out.println(ownerId + " start to acquire lock");
                boolean locked = redisDistributedLockService.lockWithRetry(
                        lockKey,
                        ownerId,
                        5000,
                        200,
                        20000
                );
                System.out.println(ownerId + " acquire lock result: " + locked);
                if (locked) {
                    try {
                        System.out.println(ownerId + " is doing business logic");
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    } finally {
                        boolean unlocked = redisDistributedLockService.unlock(lockKey, ownerId);
                        System.out.println(ownerId + " release lock result: " + unlocked);
                    }
                }
            });
        }
        executorService.shutdown();
    }
}