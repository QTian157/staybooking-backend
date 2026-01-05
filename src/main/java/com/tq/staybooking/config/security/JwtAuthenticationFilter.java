package com.tq.staybooking.config.security;

import com.tq.staybooking.util.JwtUtil;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    // 专门处理 token 的工具
        // 验 token 是不是真的
        // 从 token 里读出 username

    private final UserDetailsService userDetailsService;
    // 👉 专门根据 username 去数据库查“这个用户是谁 + 有什么角色”

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // =========================debug====================================
        System.out.println(">>> [JWT] Authorization header = " + request.getHeader("Authorization"));

        System.out.println(">>> [REQ] " + request.getMethod() + " " + request.getRequestURI());
        System.out.println(">>> [REQ] Content-Type = " + request.getContentType());
        System.out.println(">>> [REQ] Content-Length = " + request.getContentLengthLong());

        String authHeader = request.getHeader("Authorization");
        // HTTP 里，token 放在 Header 里，长这样：Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...

        // 没有 token：直接放过，交给后面的 security 规则决定是否拦截
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7).trim(); // "Bearer " 一共 7 个字符, 真正的 token 在后面

        // token 过期 / 不合法：放过，后面会变成未登录状态
        try {
            if (!jwtUtil.validateToken(token)) {
                filterChain.doFilter(request, response);
                return;
            }
        } catch (Exception e) {
            filterChain.doFilter(request, response);
            return;
        }

        //==========================debug=================================
        System.out.println(">>> [JWT] token VALID");

        UserDetails userDetails = null;
        String username = jwtUtil.extractUsername(token);

        // 🔴🔴🔴 就加在这里
        System.out.println(">>> [JWT] extracted username = " + username);
        System.out.println(">>> [JWT] will load user by username = " + username);
        // 🔴🔴🔴 到这里结束


        // 如果当前还没认证过，就建立认证信息
        // SecurityContextHolder.getContext().getAuthentication() -> Spring 判断“你有没有登录”的唯一标准
        // 这么写有逻辑漏洞：有时候 SecurityContextHolder.getContext().getAuthentication() 不是 null
        //（尤其是当线程/上下文被复用、或者你某处把 context 放到了 session/用了有状态设置时）
        //
        //于是这次请求即使 token 是 Liu，也不会覆盖掉旧的 auth → 仍然用 tq → 403。
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            userDetails = userDetailsService.loadUserByUsername(username);

            // 🔴 顺手再加两行（强烈建议）
            System.out.println(">>> [JWT] loaded user = " + userDetails.getUsername());
            System.out.println(">>> [JWT] loaded authorities = " + userDetails.getAuthorities());
            // 🔴 到这里结束

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, // principal = userDetails（你是谁）
                            null, // credentials = null（不需要密码了）
                            userDetails.getAuthorities() // authorities = 你的角色列表（你能干什么）
                    );

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication); // ✅ Spring Security 自带机制。从这一刻起，这个请求在 Spring Security 看来就是“已登录”。
        }

        //=============================debug=============================

        // ================== 🔴 就在这里加 ==================
        var auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println(">>> [SEC] auth = " + auth);
        if (auth != null) {
            System.out.println(">>> [SEC] name = " + auth.getName());
            System.out.println(">>> [SEC] authorities = " + auth.getAuthorities());
        }
        // ================== 🔴 到这里结束 ==================
        if (userDetails != null) {
            System.out.println(">>> [JWT] username = " + userDetails.getUsername());
            System.out.println(">>> [JWT] authorities = " + userDetails.getAuthorities());
        }

        filterChain.doFilter(request, response);
    }
}
/**
 * 它是一个 Filter（过滤器）
 * 你可以把 Filter 想成：
     * 🚪 门口的安检人员
     * 所有 HTTP 请求
     * 在进入 Controller 之前
     * 都会先经过它
 * 而 OncePerRequestFilter 的意思是：
     * 一次请求，只检查一次，不重复检查
 *
 * Filter 的工作不是拦截，而是“把 token 翻译成 Authentication，然后塞进 SecurityContext”。
 * SecurityConfig 才负责“拦不拦”。
 */

/**
 * 这是课件写法，适合demo
 */
//@Component
//public class JwtFilter extends OncePerRequestFilter {
//    private final String HEADER = "Authorization";
//    private final String PREFIX = "Bearer ";
//    private AuthorityRepository authorityRepository;
//    private JwtUtil jwtUtil;
//
//    @Autowired
//    public JwtFilter(AuthorityRepository authorityRepository, JwtUtil jwtUtil) {
//        this.authorityRepository = authorityRepository;
//        this.jwtUtil = jwtUtil;
//    }
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
//        // 先找到header
//        final String authorizationHeader = httpServletRequest.getHeader(HEADER);
//        String jwt = null;
//        // 如果header不空，并且开始以Bearer，就可以提取jwt了
//        if (authorizationHeader != null && authorizationHeader.startsWith(PREFIX)) {
//            jwt = authorizationHeader.substring(PREFIX.length());
//        }
//
//        if (jwt != null && jwtUtil.validateToken(jwt) && SecurityContextHolder.getContext().getAuthentication() == null) {
//            String username = jwtUtil.extractUsername(jwt);
//            Authority authority = authorityRepository.findById(username).orElse(null);
//            if (authority != null) {
//                List<GrantedAuthority> grantedAuthorities = Arrays.asList(new GrantedAuthority[]{new SimpleGrantedAuthority(authority.getAuthority())});
//
//                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
//                        username,
//                        null,
//                        grantedAuthorities
//                );
//
//                usernamePasswordAuthenticationToken.setDetails(
//                        new WebAuthenticationDetailsSource().buildDetails(httpServletRequest)
//                );
//
//                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
//            }
//        }
//        filterChain.doFilter(httpServletRequest, httpServletResponse);
//    }
//}
