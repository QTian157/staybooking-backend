package com.tq.staybooking.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.Entity;

import javax.persistence.Id;

import javax.persistence.Table;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;


import java.io.Serial;
import java.io.Serializable;


/**
 * Since we’ll use Hibernate to support database operation,
 * we need to mark the class as Entity and pick the username as ID.
 *
 * 1. Add the public Getter and Setter for each private field.
 * 2. If you want to use the builder pattern to create a User object in the future,
 * you can add a static inner class Builder to the User class and provide a private constructor for it.
 * 3. Finally add @JsonIgnore annotation to the password and enabled field.
 * Because in some services, like stay list and reservation list,
 * we want to show the host information or guest information,
 * but we only want to show the username, not password or enabled.
 */

@Entity
//change name from use to users
//✅ 原因 1：user 是很多数据库的“保留字” (reserved keyword)
//✅ 原因 2：工程规范通常使用复数表示集合
//✅ 原因 3：避免与系统内部表冲突: 很多系统里都有内建 user 表
@Table(name = "users")

@JsonDeserialize(builder = User.Builder.class)

public class User implements Serializable {
    @Serial // this is new in java, not necessary
    private static final long serialVersionUID = 1L;
    // serialVersionUID 的作用是让类在序列化 → 反序列化时保持兼容性，防止因为类结构变化导致错误。
    // 写成 1L 只是为了稳定，不让 JVM 自动生成版本号。
    @Id
    private String username;
//    @JsonIgnore
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
//    @JsonIgnore
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private boolean enabled;

//  JPA/Hibernate 必须要无参构造函数。
//  因为 Hibernate 要通过 反射 来创建实体对象。
//  它做的事情是：public User() {}
//  如果你的 class 里没有：如果你的 class 里没有：
//  Hibernate 拿不到构造方法，就会报错：No default constructor for entity
    public User(){};

//  这是你给“builder 模式”专用的构造方法。
//  这个构造方法 不应该被外部乱调用，所以通常写成 private。
    private User(Builder builder){
        this.username =builder.username;
        this.password = builder.password;
        this.enabled = builder.enabled;
    }

    public String getUsername() {
        return username;
    }

//    ✅ 为什么 setUsername 返回 User，而不是 void 或 String？
//    这是为了实现一种写法叫：👉 链式调用（Fluent API / Method Chaining）
//    User user = new User()
//            .setUsername("qi")
//            .setPassword("123456")
//            .setEnabled(true);
//    是不是看起来很流畅？像 builder 一样一条链下来。

//    ❓ 如果 setter 返回 void 会怎样？
//    常规 setter：
//    public void setUsername(String username) {
//        this.username = username;
//    }
//    你只能这样写：
//    User user = new User();
//    user.setUsername("qi");
//    user.setPassword("123456");
//    user.setEnabled(true);
//    可用，但不优雅。

//     ❓ 如果 setter 返回 String 会怎样？
//     毫无意义。
//     public String setUsername(String username) {
//        this.username = username;
//        return username; // 返回 String 有啥用？没用。
//     }
//     你没法链式调用：
//            new User().setUsername("qi").???   // 后面没法继续点 setPassword

//    ✅ 为什么返回 User 就能链式调用？
//    链式调用的核心逻辑是：
//    每个 setter 返回当前对象本身（this）。
//
//    public User setUsername(String username) {
//        this.username = username;
//        return this;    // 返回自己，让下一次 .setXX() 继续用
//    }
//    这样 .setUsername() 的返回值就是一个 User 类型，
//    所以你可以继续：.setPassword(...).setEnabled(...)

//    🧠 一句话总结：
//    setter 返回 User，是为了支持链式调用，让代码更简洁、更像构建器（builder）模式。

    public User setUsername(String username) {
        this.username = username;
        return this;
    }
    public String getPassword() {
        return password;
    }

//    ❓setter 是怎么用的？没有一个 User 就不能用了？
//    你写的 setter 都是“实例方法”（没加 static）
//    必须先 new 出对象才能调用：
//    new User().setUsername("qi")
//    new User.Builder().setUsername("qi")
//    不能 在没有对象的情况下直接调用：User.setUsername("qi") ❌

    public User setPassword(String password) {
        this.password = password;
        return this;
    }

    public boolean isEnabled(){
        return enabled;
    }
    public User setEnabled(boolean enabled){
        this.enabled = enabled;
        return this;
    }
// 静态类static 可以直接在User.Builder直接setter
//    ✅ Builder 的 setter：
//    你是在 准备创建一个新的 User
//    User 还没被 new 出来
//    只是给 Builder 这个“草稿本”填内容
// User user = new User.Builder()
//        .setUsername("qi")
//        .setPassword("123")
//        .setEnabled(true)
//        .build();              // 这一步才真正 new User

//    ✅ User 的 setter：
//    对象已经创建好（可能已经从数据库查出来）
//    你在 修改一个已经存在的用户
// User user = userRepository.findById("qi").get();
// user.setEnabled(false);  // 改成禁用

    public static class Builder{
        // 这里的annotation是前端和builder的映射
        @JsonProperty("username") // JSON 必须是：{ "username": "qi" } 才能映射
        private String username;

        @JsonProperty("password")
        private String password;

        @JsonProperty("enabled")
        private boolean enabled;

        public Builder setUsername(String username){
            this.username = username;
            return this;
        }
        public Builder setPassword(String password){
            this.password = password;
            return this;
        }

        public Builder setEnabled(boolean enabled){
            this.enabled = enabled;
            return this;
        }
        public User build(){
            return new User(this);
        }
//        ❓ build() 里的 this 怎么找的？
//        写在 Builder 里面 → this 就是当前的 Builder 对象
//        new User(this) 等价于 new User(当前这个 builder)
//        User 的构造方法接收 Builder，把里面的 username/password/enabled 拷贝出来

    }
}

// 为什么在 JPA 的实体类（Entity）里常常会 implements Serializable？
//✅ 1. Serializable 是干嘛的？
//Serializable 的作用就是：
//让这个对象可以被转换成一串字节（byte stream），然后再从字节恢复成为对象。
//
//也就是：
//对象 → 字节：可以被保存进缓存、session、Redis、文件、网络传输……
//字节 → 对象：恢复回来继续用
//
//实体类实现 Serializable 是为了让它能被缓存、网络传输、放到 session 中，并避免各种框架报错。不是必需，但很常见的习惯做法。

//✅ 更准确地说：Serializable不是给前端用，后端用，框架用
//Serializable = 让这个对象能在 Java 的世界里坐“传送带”跑来跑去。
//        （1）你把 User 放进 HttpSession：
//        （2）Hibernate 的二级缓存需要把实体保存成字节 → 必须是 Serializable
//        （3）分布式系统，服务器 A 要把对象传给服务器 B → 必须是 Serializable

// 🔍 那为什么 Entity 常常加它？
//因为：
//☑ 很多框架会“悄悄地”把实体存储或传输
//☑ 为了避免后期遇到奇怪错误
//☑ 所以团队通常要求：所有 Entity 都加 Serializable
//是一种“保险做法”。

//最终要做的事情就是：
//把前端 JSON 的 username → 存到数据库表的 username 字段。
//只是这个过程不是直接做，而是分成三步走：
//【前端 JSON】
//     "username" : "qi"
//        ↓ (Jackson 看到 @JsonProperty)
//【Builder.username = "qi"】
//        ↓ (builder.build())
//【User.username = builder.username】
//        ↓ (JPA @Column/@Id)
//【数据库字段 username = "qi"】

//🔥 那为什么要绕这么一圈？为什么不 JSON → User 直接存？
//        | 直接方式                       | 问题                     |
//        | -------------------------- | ---------------------- |
//        | JSON → User（setter）        | 容易破坏实体、难以校验、安全风险大      |
//        | JSON → User（全参构造）          | 字段多时代码难读、容易错、无法灵活控制    |
//        | JSON → User（无参构造 + setter） | 无法保证对象完整性，也容易创建“半成品对象” |
