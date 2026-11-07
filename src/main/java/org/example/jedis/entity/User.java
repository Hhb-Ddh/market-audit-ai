package org.example.jedis.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String name;
    private String age;

    // ❌ 旧代码：没有时间字段
    // private String createTime;

    // ✅ 新代码：新增 LocalDateTime 时间字段，用于验证序列化格式
    private LocalDateTime createTime;

    // 构造方法
    public User(String name, String age) {
        this.name = name;
        this.age = age;
        // ❌ 旧代码：无时间赋值
        // this.createTime = null;

        // ✅ 新代码：自动赋值为当前时间
        this.createTime = LocalDateTime.now();
    }
}