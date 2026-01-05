package com.tq.staybooking.config;

import com.tq.staybooking.config.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.sql.DataSource;

/**
 * Besides the duplicated username checking,
 * we also need to take care of the password management.
 * For security reasons, storing unencrypted passwords directly in the database is not recommended.
 * We should do the encryption before saving the data.
 * Create a new package named com.tq.staybooking.config
 * and add a new class called SecurityConfig to it.
 */

@Configuration
// /可写可不写，
// 老版本默认不会开启 Web Security，
// 新版本只要你的项目里依赖了 spring-security， 它就会自动开启 Web Security，不需要手动写 @EnableWebSecurity
@EnableWebSecurity
// extends WebSecurityConfigurerAdapter 已经被取消了
public class SecurityConfig {

    // 1. 密码加密器 —— 现在都必须要有
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

//    @Override
//    protected void configure(HttpSecurity http) throws Exception{
//        http
//                .authorizeRequest()
//                .antMatchers(HttpMethod.POST, "/register/*").permitAll()
//                .anyRequest().anthenticated()
//                .and()
//                .csrf()
//                .disable();
//    }
//    @Bean
//    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
//        http
//                .csrf(csrf -> csrf.disable())  // ⭐ 新版本必须这么写
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers(HttpMethod.POST, "/register/*").permitAll()
//                        .requestMatchers(HttpMethod.POST, "/authenticate/*").permitAll()
//                        .requestMatchers("/stays").permitAll()
//                        .requestMatchers("/stays/**").permitAll()
//
//                        // stay 管理必须登录
////                        .requestMatchers(HttpMethod.GET, "/stays/**").hasRole("HOST")
////                        .requestMatchers(HttpMethod.POST, "/stays/**").hasRole("HOST")
////                        .requestMatchers(HttpMethod.DELETE, "/stays/**").hasRole("HOST")
//
//                        .anyRequest().authenticated()
//                );
//        return http.build();
//    }

//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http
//                .authorizeRequests()
//                .antMatchers(HttpMethod.POST, "/register/*").permitAll()
//                .antMatchers(HttpMethod.POST, "/authenticate/*").permitAll()
//                .antMatchers("/stays").hasAuthority("ROLE_HOST")
//                .antMatchers("/stays/*").hasAuthority("ROLE_HOST")
//                .anyRequest().authenticated()
//                .and()
//                .csrf()
//                .disable();
//        http
//                .sessionManagement()
//                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 👉 明确告诉 Spring：不要创建 HttpSession，也不要用 Session 存登录状态
//                .and()
//                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);


// 这是boot3以上的用法
//    @Bean
//    public SecurityFilterChain configure(HttpSecurity http,
//                                         JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
//        http
//                .csrf(csrf -> csrf.disable())
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers(HttpMethod.POST, "/register/*").permitAll()
//                        .requestMatchers(HttpMethod.POST, "/authenticate/*").permitAll()
//                        .requestMatchers("/stays/**").hasRole("HOST") //这里自动匹配ROLE_HOST
//                        .requestMatchers("/stays/**").hasRole("USER")
////                        .requestMatchers("/stays/**").hasAuthority("ROLE_HOST")
//                        .anyRequest().authenticated()
//                )
//                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); //这个是真正做token认证

        //这是教案的jwt的做法
//        http
//                .sessionManagement()
//                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                .and()
//                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

//        return http.build();
//    }

    // ✅ 方法参数注入jwtAuthenticationFilter
    // JwtAuthenticationFilter 已经是一个 Bean（@Component）
    // Spring 在创建 SecurityFilterChain 时
    // 自动把它作为参数注入进来
    // **在 Spring 里：
        //@Component / @Service / @Bean → 把对象放进容器
        //@Bean 方法参数 / 构造函数参数 → 从容器里“要对象”**


    @Bean
    public SecurityFilterChain configure(HttpSecurity http,
                                         JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                .csrf().disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                    .antMatchers(HttpMethod.POST, "/register/*").permitAll()
                    .antMatchers(HttpMethod.POST, "/authenticate/*").permitAll()

                    // 先别写两个 hasRole 都针对同一个 /stays/**（会冲突）
                    // 先让 stays 只要求登录 or 只给 HOST，二选一
//                    .antMatchers("/stays/**").authenticated()
                    .antMatchers("/stays/**").hasRole("HOST")
                    .antMatchers("/search").hasRole("GUEST")
                    .antMatchers("/reservations").hasRole("GUEST")
                    .antMatchers("/reservations/**").hasRole("GUEST")
                    .anyRequest().authenticated()
                .and()
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 为什么 STATELESS 一加就“神奇好了”: 要不然每次请求 即使token对了 也不行 偶尔混乱不知道用的host还是guest
     * 这句话告诉 Spring Security：
         * ❌ 不要创建 Session
         * ❌ 不要复用上一次请求的 SecurityContext
         * ✅ 每个请求都必须 完全靠 Header 里的 token 来认证
     * 结果就是：
     * 每个请求进来：
         * SecurityContext 一定是空的
         * 你的 JwtFilter 一定会重新 setAuthentication
         * 不存在“上一个请求污染下一个请求”
     * 👉 这正是 JWT 的设计初衷。
     */

    @Autowired
    private DataSource dataSource;
//    @Override
//    protected void configure(AuthenticationManagerBuilder auth) throws Exception{
//        auth.jdbcAuthentication().dataSource(dataSource)
//                .passwordEncoder(passwordEncoder())
//                .usersByUsernameQuery("SELECT username, password, enabled FROM user WHERE username = ?")
//                .authoritiesByUsernameQuery("SELECT username, authority FROM authority WHERE username = ?");
//    }
// In addition to configuring the datasource for AuthenticationManager,
// we also need to expose it as a bean so that we can use it in our authentication service.
// We didn’t do it in the first project because we used session-based authentication with http.loginForm()
// provided by Spring Security.
//    @Override
//    @Bean
//    public AuthenticationManager authenticationManagerBean() throws Exception {
//        return super.authenticationManagerBean();
//    }

    // 2. UserDetailsService（采用 JDBC + 自定义 SQL）
    // -> 用 JDBC 的 UserDetailsService，等价于老版的 auth.jdbcAuthentication()
    // UserDetailsService = “给我一个 username，我给你一个 UserDetails”
    // UserDetailsService 会在什么时候被用到？
    // ✅ 情况 1：登录时（AuthenticationManager.authenticate）
        // authenticationManager.authenticate(...)
        // Spring 内部会：
            // -> 调 userDetailsService.loadUserByUsername(username)
            // -> 拿到 password + authorities
            // -> 用 PasswordEncoder 比对密码
    // ✅ 情况 2：带 token 的请求（JwtAuthenticationFilter）
        // userDetailsService.loadUserByUsername(username);
        // 你在 Filter 里 手动调用它，目的只有一个：从数据库重新拿到这个用户的角色列表（authorities）
    @Bean
    public UserDetailsService userDetailsService(){
        //JdbcUserDetailsManager =“如果你的用户存在数据库里，我可以帮你查，但你要告诉我 SQL”
        JdbcUserDetailsManager manager = new JdbcUserDetailsManager(dataSource);
        // 这两个 SQL 就是你之前 configure() 里写的那两个
        manager.setUsersByUsernameQuery(
                "SELECT username, password, enabled FROM users WHERE username = ?"
        );
        manager.setAuthoritiesByUsernameQuery(
                "SELECT username, authority FROM authority WHERE username = ?"
        );
        return manager; // ❗❗ 一定要记得 return
    }


    // -> 暴露 AuthenticationManager（给 service/controller 注入用）
    // AuthenticationManager = “给我一个 username + password，我告诉你：是不是合法用户”
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception{
        return configuration.getAuthenticationManager();

    }
    // AuthenticationConfiguration 是 Spring Security 自动放进去的一个 Bean。
        // -> 它是一个“配置中心”，帮你生成 AuthenticationManager。
        // -> Spring Security，你帮我拿你内部创建好的 AuthenticationManager，我要把它暴露成 Bean。
        // -> 以前的老版本你需要写: return super.authenticationManagerBean();
        // -> 新版本没有父类了，换成: 新版本没有父类了，换成
    // “Spring，你已经帮我把 AuthenticationManager 配好了（包括 UserDetailsService、PasswordEncoder 等），现在请把它‘暴露出来’，让我能注入使用。”
}
// ✅ BCryptPasswordEncoder() 本身不是“加密方法”，而是“加密器（一个工具类）”。
// 它的作用是：👉 提供一个可以 加密密码 和 校验密码 的工具。
// 你真正用来加密密码的方法是它里面的：passwordEncoder.encode(明文密码)
    // -> encode() = 把明文密码打碎 → 生成随机不可逆字符串
    // -> matches() = 检查用户输入的密码是否能通过验证
//🎯 你现在对加密流程应该清楚了：
//        | 步骤      | 内容                         |
//        | -----    | --------------------------- |
//        | 创建加密器 | new BCryptPasswordEncoder() |
//        | 加密      | encode()                    |
//        | 登录验证   | matches()                   |


/**
 * SecurityConfig  = 安全设置专用的配置类
 * 我分三层来讲：
     * SecurityConfig 负责管“安全相关”的东西
     * 里面一般会放哪些 Bean？
     * 你现在这个练习项目，最少需要放什么就够了？
 * 1️⃣ SecurityConfig 是干嘛的？SecurityConfig = 专门放“Spring Security 配置”的地方。
     * 👉 只放那些跟“登录 / 认证 / 权限 / 密码 / 安全规则”有关的东西。
     * 所以你项目里可以有很多 config 类，比如：
         * JpaConfig：管数据库相关
         * WebConfig：管跨域、拦截器、静态资源映射
         * SecurityConfig：管 Spring Security 和安全策略
 * 2️⃣ SecurityConfig 里面“常见会放的东西”有哪些？
     * ✅ 1. 密码加密器（你已经有了）
     * @Bean
     * public PasswordEncoder passwordEncoder() {
     *     return new BCryptPasswordEncoder();
     * }
     * 注册时加密密码/ 登录时验证密码（用 matches()）
     *✅ 2. 安全过滤链（SecurityFilterChain）——控制“哪些接口要登录”
     * @Bean
     * public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
     *     http
     *         .csrf().disable()
     *         .authorizeHttpRequests(auth -> auth
     *             .requestMatchers("/register", "/login").permitAll()  // 这些接口谁都能访问
     *             .anyRequest().authenticated()                       // 其他都要登录
     *         )
     *         .formLogin();  // 用表单登录（用户名/密码）
     *     return http.build();
     * }
     * 能做的事包括：
         * 哪些 URL 不用登录就能访问（permitAll()）
         * 哪些 URL 需要特定角色（.hasRole("HOST")）
         * 是否使用表单登录 / HTTP Basic / JWT
         * 是否开启 CSRF、CORS、Session 之类
     * ✅ 3. 用户信息来源（UserDetailsService / AuthenticationProvider）
     * 比如你想用数据库里的 User 表做登录认证，可以配一个：
     * @Bean
     * public UserDetailsService userDetailsService(UserRepository userRepository) {
     *     return username -> userRepository.findById(username)
     *             .orElseThrow(() -> new UsernameNotFoundException("User not found"));
     * }
     * ✅ 4. JWT 相关配置（如果你以后做 token 登录）
     * 比如：
         * 用来验证 token 的过滤器
         * 配置哪些接口需要带 token
     * 这类东西通常也会和 SecurityFilterChain 放一起。
 * 3️⃣ 那 SecurityConfig 里面是不是“只放自定义 Bean”？
     * 是的：你这里写的基本上都是你项目自己定义的 Bean（PasswordEncoder、SecurityFilterChain、UserDetailsService 等），告诉 Spring Security “你要用这些配置”
     * 但有些是你“配置”已有的东西，比如你给 HttpSecurity 写规则，也算是在“自定义行为”
     * SecurityConfig 里放的是：“安全相关的 Bean + 安全规则配置”，而不是所有乱七八糟的 Bean。
 */

/** 3️⃣ SecurityFilterChain全局用 不在register用
 *  给你一个真实请求流程（比方说前端发一个 POST /register/user）：
 *  -> 浏览器/前端 发请求：POST /register/user
 *  -> 请求先经过：✅ Spring Security 的过滤器（Filter Chain）
 *  -> Filter 会根据你的 configure(HttpSecurity http) 的配置判断：
     *  这个 URL 是 /register/*，而且是 POST
     *  配置写了 .antMatchers(HttpMethod.POST, "/register/*").permitAll()
 *  👉 所以：允许通过，不用登录
 *  -> 然后才把请求交给：RegisterController.register()
 *  -> 如果是别的接口，比如：GET /house/list
 *  -> 不在 /register/* 范围里
 *  -> 就会被 .anyRequest().authenticated() 命中
 *  👉 必须登录，否则返回 401 / 403
 *  也就是说：SecurityConfig 是在请求进入 Controller 之前，就已经先拦一遍了。
 */

/**
 * 老版本的authentication验证
 * 我们把它分两大块讲：
     * -> 配置“怎么认证” → configure(AuthenticationManagerBuilder auth)
     * -> 把 AuthenticationManager 暴露出来让别人用 → authenticationManagerBean()
 * 🚀 Part 1：configure(AuthenticationManagerBuilder auth) —— 告诉 Spring Security 登录应该怎么查数据库
 * Spring Security，我告诉你：用户要登录的时候，你应该去哪里查账号、密码、权限。
     * 🌟 第一步：auth.jdbcAuthentication(): “我不用内存、也不用写死的 UserDetailsService，我要用数据库来认证用户。”
     * 🌟 第二步：.dataSource(dataSource): “登录时的 SQL 查询都用这个 MySQL 连接去执行。” dataSource 是你 application.properties 里配置的：
     * 🌟 第三步：.passwordEncoder(passwordEncoder()): “数据库里存的是 BCrypt 加密后的密码。你验证密码的时候不能直接比对，要用 BCryptPasswordEncoder 来解密比对。”
     * 🌟 第四步：usersByUsernameQuery():
     * -> .usersByUsernameQuery("SELECT username, password, enabled FROM user WHERE username = ?")
     * -> 这是登录第一步：根据用户名查信息, 这个 SQL 作用是：
         * 检查用户名是否存在
         * 得到数据库里的密码（加密过的）
         * 得到 enabled 是否为 true
     * -> Spring Security创建：返回的数据会被封装成一个 UserDetails 对象的“账号信息部分”。
     * 🌟 第五步：authoritiesByUsernameQuery()
     * -> .authoritiesByUsernameQuery("SELECT username, authority FROM authority WHERE username = ?")
     * 这个 SQL 查的是：
         * 这个用户有哪些角色（ROLE_GUEST / ROLE_HOST）
         * 登录成功后 Spring Security 才知道这个用户能访问哪些接口
     * 登录流程是这样：
         * 先查：users 表 → 判断账号密码是否正确
         * 再查：authority 表 → 加载角色
 * 🚀 Part 2：authenticationManagerBean() —— 把 AuthenticationManager 暴露为 Bean
     * ⭐ AuthenticationManager 是“登录的大脑”
         * 当你想自己写一个登录接口，比如:
         * @PostMapping("/login")
         * public String login(@RequestBody LoginRequest req) {
         *     Authentication auth = authenticationManager.authenticate(
         *         new UsernamePasswordAuthenticationToken(req.username, req.password)
         *     );
         * }
         * 你会发现：❗ AuthenticationManager 自动注入不了！因为老版本 Spring Security 默认不会把它放进 Spring 容器里。所以需要你手动暴露它：
     * ⭐ 你写的这个方法，就是把父类创建好的 AuthenticationManager 变成可注入的 Bean：
         * @Override
         * @Bean
         * public AuthenticationManager authenticationManagerBean() throws Exception {
         *     return super.authenticationManagerBean();
         * }
     * “Spring，请把父类帮我配置好的 AuthenticationManager 公开出来，我要在 Controller/Service 里用它来验证密码。”
     * 写了这个，你就可以在任何地方：
         * @Autowired
         * AuthenticationManager authenticationManager;
 * 🔥 超简化流程图（你一看就懂）
     * 用户输入用户名 + 密码
     *         ↓
     * authenticationManager.authenticate()
     *         ↓
     * 执行 configure(auth) 里配置的两条 SQL
     *         ↓
     * 查数据库账号 + 密码
     *         ↓
     * BCrypt 比对密码
     *         ↓
     * 成功 → 加载权限 → 登录成功
     * 失败 → 抛 BadCredentialsException
 * ⭐ 你整个 Spring Security 登录过程不需要 new 一个对象，全部自动完成
     *             Spring Security 登录
     * ────────────────────────────────────
     * Step 1: 执行 SQL（查用户信息）
     * Step 2: 执行 SQL（查权限）
     * Step 3: spring 自动 new UserDetails(username, password, enabled)
     * Step 4: spring 自动加上 authorities
     * Step 5: spring 自动用 passwordEncoder.matches() 比对密码
     * Step 6: spring 自动把 UserDetails 放入 SecurityContextHolder
 * ⭐ 你真正要记住的一句话: UserDetails 是由 Spring Security 自动根据你的 SQL 查询结果组装出来的，你完全不需要创建这个对象。
 *
 */

/**
 * HTTP 请求
 *    ↓
 * 登录（username + password）
 *    ↓
 * AuthenticationManager.authenticate()
 *    ↓
 *     ↳ 使用 UserDetailsService 查数据库
 *     ↳ 使用 PasswordEncoder 校验密码
 *    ↓
 * Authentication（已认证，含角色）
 *    ↓
 * JwtUtil.generateToken()
 */

/**
 * HTTP 请求（带 token）
 *    ↓
 * JwtAuthenticationFilter
 *    ↓
 * extractUsername(token)
 *    ↓
 * UserDetailsService.loadUserByUsername(username)
 *    ↓
 * Authentication（含角色）
 *    ↓
 * SecurityContext
 *
 */