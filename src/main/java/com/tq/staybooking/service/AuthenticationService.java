package com.tq.staybooking.service;


import com.tq.staybooking.exception.UserNotExistException;
import com.tq.staybooking.model.Token;
import com.tq.staybooking.model.User;
import com.tq.staybooking.model.UserRole;
import com.tq.staybooking.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

/**
 * 1. Go to com.tq.staybooking.service package and create a new class AuthenticationService.
 * 2. Add the AuthenticationManager, AuthorityRepository,
 * and JwtUtil as the private field and create a constructor for initialization.
 * 3. Go back to the AuthenticationService
 * and add the authenticate method to check the user credential and return the Token if everything is OK.
 */
@Service
public class AuthenticationService {
    private AuthenticationManager authenticationManager;
    private JwtUtil jwtUtil;

    @Autowired
    public AuthenticationService(AuthenticationManager authenticationManager, JwtUtil jwtUtil){
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    public Token authenticate(User user, UserRole role) throws UserNotExistException {
        Authentication auth = null;
        try{
            auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));

        }catch(AuthenticationException e){
            throw new UserNotExistException("Invalid username or password");

        }
//        if (auth == null || !auth.isAuthenticated() || !auth.getAuthorities().contains(new SimpleGrantedAuthority(role.name())) ){
//            throw new UserNotExistException("User Doesn't Exist");
//        }
        if (auth == null || !auth.isAuthenticated()) {
            throw new UserNotExistException("Invalid username or password");
        }
// SimpleGrantedAuthority 是 Spring Security 官方提供的“权限/角色的最简单实现”。
// SimpleGrantedAuthority 是 Spring Security 自带的角色/权限封装类，用来表示用户的某个权限（比如 ROLE_GUEST），通常和 auth.getAuthorities() 一起用来做权限匹配。
// 它表示一个权限或角色标签，比如：
//        "ROLE_GUEST"
//        "ROLE_HOST"
//        "ROLE_ADMIN"
//        "booking:write"
//        "order:read"
// new SimpleGrantedAuthority("ROLE_GUEST")
// Spring Security 在内部存角色时不是用 String，而是用 GrantedAuthority 对象。

        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority(role.name()))) {
            throw new UserNotExistException("User does not have required role: " + role.name());
        }


        return new Token(jwtUtil.generateToken(user.getUsername()));
    }
}

/**
 * “如何拿到前端的 auth / token” 这件事怎么串起来？
 * 分成两段人生来看：登录时 vs 之后每次请求。
 * ① 登录那一刻（你现在这段代码做的事）
     * 1. 前端发 /login 请求，带上 username/password
     * 2. Controller 接收成一个 User 对象，或者 DTO
     * 3. Controller 调用：Token token = authenticationService.authenticate(user, UserRole.ROLE_GUEST);
     * 4. 你的 AuthenticationService.authenticate(...) 内部：
         * -> 用 authenticationManager.authenticate(...) 验证用户名密码 + 角色
         * -> 如果失败 → 抛 UserNotExistException
         * -> 如果成功 → jwtUtil.generateToken(user.getUsername()) 生成 JWT
         * -> return new Token(jwtString);
         * -> Controller 把这个 Token 对象序列化成 JSON 返回给前端，比如：{ "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." }
     * ✅ 这一步结束后：“前端就拿到了 token”。
 * ② 之后每次请求，前端拿着 token 来访问别的接口
 * 例如请求 /rooms 之类的接口时，前端会在 HTTP header 里加：Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
 * 后端一般会有一个 JWT 过滤器（你们教程里应该也会创建一个，比如 JwtFilter），这个过滤器会：
     * -> 从请求头里拿 Authorization，截取出 Bearer 后面的 token 字符串
     * -> 调用 jwtUtil.extractUsername(token) 拿到用户名
     * -> 调 UserDetailsService.loadUserByUsername(username) 再查一次数据库拿 UserDetails
     * -> 如果 jwtUtil.validateToken(token, userDetails) 通过，就 new 一个：
     * Authentication auth =
     *     new UsernamePasswordAuthenticationToken(
     *         userDetails,
     *         null,
     *         userDetails.getAuthorities()
     *     );
     * -> 并且：SecurityContextHolder.getContext().setAuthentication(auth);
 * 这样：后面 Controller 里如果需要当前登录用户，就可以通过：
     * -> SecurityContextHolder.getContext().getAuthentication()
     * -> 或者在方法参数上用 @AuthenticationPrincipal 注入 UserDetails
 */

/**
 * Token authenticate(User user, UserRole role)这个function实际上就是在做验证登录成功与否；
     * -> 用 authenticate 方法是为了验证登录成功与否；
     * -> 成功就 return new Token(jwtUtil.generateToken(user.getUsername())); 返回。给前端放在 Bearer 里；
     * -> 中间用到 Authentication，通过 isAuthenticated、role 判断；
     * -> 失败就抛异常，当成“用户不存在”；
     * -> 成功就 return new Token(jwtUtil.generateToken(user.getUsername())); 返回。
 * Authentication 这个对象的“加密”不是重点，它本身就只是一个结果对象，主要关注：
     * -> principal（通常是 UserDetails）
     * -> authorities（角色）
     * -> isAuthenticated()（是否通过验证）
 */

/**
 * 如何拿到前端的 auth（其实就是登录后的 JWT）
 * 1. new Authentication auth
 * 2. auth = authenticationManager.authenticate(username, password)
     * -> 要用 authenticationManager.authenticate（Spring 自带）需要用户名密码，
     * -> 用 UsernamePasswordAuthenticationToken 包一下
         * -> 刚 new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())的时候
         * -> authenticated=false，名字密码来自前端+controller
         * -> 这一步不会触发查数据库，就是你把“用户名+密码”这张纸塞给保安而已。
 * 3. 交给 authenticationManager.authenticate，Spring 自动做认证 “Spring 自动做认证，匹配就把 authenticated 设置成 true，同时组装了 UserDetails 完整信息。”
     * -> Spring 内部大致流程：
     * AuthenticationManager (ProviderManager)
     *   -> DaoAuthenticationProvider
     *      -> UserDetailsService.loadUserByUsername(username)   // 连数据库查 users/authority
     *      -> PasswordEncoder.matches(rawPwd, encodedPwd)       // BCrypt 验证密码
     *      -> 组装一个新的 Authentication（里面是 UserDetails + 权限 + authenticated=true）
     * -> 所以： authenticationManager.authenticate(...) 会
         * ① 调用 UserDetailsService 查数据库拿到 UserDetails
         * ② 用 PasswordEncoder 验证密码
         * ③ 如果通过，返回一个“带有完整 UserDetails + 权限，且 authenticated=true”的 Authentication 对象
 *
 */