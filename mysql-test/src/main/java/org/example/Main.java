package org.example;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.example.entity.User;
import org.example.mapper.UserMapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
@MapperScan(value = "org.example.mapper")
public class Main {
    @Resource
    private UserMapper userMapper;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @PostConstruct
    private void test() {
        List<User> users = userMapper.selectAll();
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        users.forEach(user -> System.out.println(user.toString()));
    }
}