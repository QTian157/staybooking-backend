package com.tq.staybooking.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;

/**
 * Create a new package called com.tq.staybooking.util and add a new class JwtUtil to it.
 * Open the application.properties file and add a new variable named jwt.secret. We’ll use the value of the jwt.secret for JWT generation. You can use any string as the value.
 * Go back to the JwtUtil class, add a private field secretKey.
 * Add a method to generate the JWT and return the encrypted result of it.
 * Next to the generateToken() method, add methods to decrypt a JWT from the encrypted value.
 */

/**
 * JwtUtil它的职责（唯一）
     * 造 token（签名、过期、写入 subject=username）
     * 验 token（验证签名+过期）
     * 从 token 取 username
 */

@Component
public class JwtUtil {
    // ✅ $ 的含义：告诉 Spring “这里面是一个要从配置文件读取的变量”
        // 这叫 Spring 的占位符语法（placeholder）。
        // “Spring 去 application.properties 找这个名字的配置，并把值替换进来。”
    @Value("${jwt.secret}")
    private String secret; // 注意：这里 application.properties 里要写 jwt.secret，不要拼成 secrete 哦

    /**
     * 把字符串 secret 转成真正的签名 Key
     * HS256 要求 key 至少 256 bit（32 字节）,
     * 所以 secret 字符串你最好写得长一点。
     */
    private Key getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
//    public String generateToken(String subject){
//        return Jwts.builder()
//                .setClaims(new HashMap<>())  // token 内容 (你现在用空的)
//                .setSubject(subject)      // token 绑定的用户名
//                .setIssuedAt(new Date(System.currentTimeMillis()))  // 什么时候生成
//                .setExpiration(new Date(System.currentTimeMillis() + 1000*60*60*24)) // 有效期 24 小时
//                .signWith(SignatureAlgorithm.HS256, secret) // 用你的 secret 加密
//                .compact();
//    }
    // 生成 token
    public String generateToken(String subject) {
        return Jwts.builder()
                .setClaims(new HashMap<>())                      // 自定义的 payload（你现在用空 map）
                .setSubject(subject)                             // 这里放 username, 把“你是谁”写进 token（你选择写 username）
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()
                        + 1000 * 60 * 60 * 24))           // 24 小时
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // 防伪：服务器用 secret 签名，别人造不出来
                .compact();
}

//    private Claims extractClaims(String token){
//        return Jwts.parser().setSigningKey(secret)
//                .parseClaimsJws(token).getBody();
//    }

    // 解析 token 得到 Claims
    // Claims = JWT 里那一小段“内容区（payload）”，是一个“已经被服务器验证过、可以信任的 Map”。
    // 一个 JWT token，本质是 3 段字符串拼在一起：xxxxx.yyyyy.zzzzz -> Header . Payload . Signature
        // -> Header：算法信息（HS256 之类）
        // -> Payload：真正的“内容”
        // -> Signature：防伪签名
    // 👉 Claims 就是 Payload 这一段被解析后的结果
        // {
        //    "sub": "qi",
        //        "iat": 1700000000,
        //        "exp": 1700003600
        //}
    // Claims 是一个接口，本质就是：Map<String, Object>
    private Claims extractClaims(String token) {
        return Jwts
                .parserBuilder()    // ⬅️ 新版本必须用 parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token) // 它一次性做了 3 件事（这是“逻辑必然”，不是 Spring 规定）：1️⃣ 验证签名2️⃣ 检查结构是否合法3️⃣ 解析 payload(把 payload JSON 转成一个 Java Map（Claims））
                .getBody();
    }

    public String extractUsername(String token){
        return extractClaims(token).getSubject();
    }
    public Date extractExpiration(String token){
        return extractClaims(token).getExpiration();
    }
    public boolean validateToken(String token){
        return extractExpiration(token).after(new Date());
    }
}

// 用户登录（/login） → 校验用户名密码 → 生成 JWT token → 返回给前端
// 1️⃣ JwtUtil：负责“做一枚加密的令牌（token）”
    // subject = username
    // secret = 加密 token 的盐（在 application.properties 设置的 jwt.secret）
    // expiration = token 什么时候失效
    // 最后 .compact() → 变成字符串 token（像：eyJhbGciOiJIUzI1NiIs....）
// 2️⃣ Token class：只是包装一下 token 字符串
    // Token 类完全不是做逻辑，是为了返回给前端一个结构：
    // 🔥 前端收到：
        //{
        //        "token": "eyjasdf...."
        //        }
// 📌 你现在整个流程已经长这样:
    // -> Step 1：前端 POST /login: 带 username + password
    // -> Step 2：后端: AuthenticationManager.authenticate() 校验用户密码
    // ->Step 3：如果成功: JwtUtil.generateToken(username) → 生成 token
    // -> Step 4：返回给前端: { "token": "xxxxxx" }
    // -> Step 5：前端之后访问 API: 在 header 里带上：Authorization: Bearer xxxxxx

// 👉 为什么 setSubject(subject) 就能被后端精准当成 username？
    // ->因为 JWT 的“subject（sub）字段”在所有认证系统里，就定义为“这个 token 是为谁发的（通常是 username）”。
    // -> subject 在 JWT 标准里本来就是专门用来存储用户身份（user identity）的字段。
    // -> Spring Security（或你写的 JwtFilter）解析 token 时会这样拿：String username = claims.getSubject();
// 🔍 1. JWT 标准里，subject（sub）是什么？
    // JWT（JSON Web Token）官方标准里定义了几个关键字段：
    //        | 字段     | 含义                     |
    //        | ------- | ---------------------- |
    //        | **sub** | 这个 token 是给谁的 → 通常是用户名 |
    //        | **iat** | 什么时候签发的                |
    //        | **exp** | 什么时候过期                 |
    //        | **iss** | 谁发的                    |
    //        | **aud** | 发给谁的（audience）         |
    //

// JWT 并不是用来登录的，而是用来“证明：这个请求是谁发的、是否还有效”。
// 所以当后端收到“一个带 token 的请求”时，它必须检查：
    // -> token 是否真实有效（没有过期、没有被篡改）
    // -> 这个 token 代表的用户是谁
    // -> 要不要把这个用户标记为“已认证”
// 这就是 extractUsername / extractExpiration / validateToken 的作用。