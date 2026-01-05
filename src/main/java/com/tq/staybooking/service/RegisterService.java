package com.tq.staybooking.service;


import com.tq.staybooking.exception.UserAlreadyExistException;
import com.tq.staybooking.model.Authority;
import com.tq.staybooking.model.User;
import com.tq.staybooking.model.UserRole;
import com.tq.staybooking.repository.AuthorityRepository;
import com.tq.staybooking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Go to com.tq.staybooking.service package. Create a new class called RegisterService.
 * 1. Add both UserRepository and AuthorityRepository as the private field of the RegisterService.
 * 2. Add a constructor for RegisterService. Don’t forget to add the @Autowire annotation to the constructor,
 * otherwise Spring won’t help you find the correct implementation of UserRepository and AuthorityRepository.
 * 3. Now add a simple method to insert new user and authority records to the database.
 * Pay attention to the @Transactional annotation,
 * it will make the insert operations to user and authority tables will succeed together, or fail together.
 * 4. Now add a simple method to insert new user and authority records to the database.
 * Pay attention to the @Transactional annotation,
 * it will make the insert operations to user and authority tables will succeed together, or fail together.
 */

// @Service —— 这是一个“服务类”
//@Service 告诉 Spring：
//        “这是一个业务逻辑的类，你之后可以注入使用。”
//比如 Controller 调用它来完成“注册用户”这件事情。

@Service
public class RegisterService {
    // 数据库操作工具
    // 两个 Repository：
        // 1. userRepository → 负责“用户表”
        // 2. authorityRepository → 负责“角色/权限表”
    //Repository 的功能就是: 让你可以不用写 SQL，就能把数据存进数据库。

    private UserRepository userRepository;
    private AuthorityRepository authorityRepository;

    private PasswordEncoder passwordEncoder;

    // constructor + @Autowired —— 把数据库工具传进来
    // 它告诉 Spring：“创建 RegisterService 的时候，请把 UserRepository 和 AuthorityRepository 这两个对象注入给我，我需要使用它们。”
        // -> RegisterService 想使用两个工具（两个 repository）
        // -> 但它自己不能 new
        // -> 必须由 Spring 帮它创建并传进来
    // 🛠️ 为什么 RegisterService 不能自己 new UserRepository？
    // 如果你写： this.userRepository = new UserRepository();
    // 是错的，因为：
    // -> Repository 是 Spring 自动生成的（代理对象）
    // -> Repository 可能需要数据库连接、事务管理等 Spring 才能提供
    // -> 你 new 出来的不受 Spring 管理，无法实现注入、事务、代理功能

    // 💡 那 @Autowired 的作用是什么？
    // 在构造函数上加 @Autowired 表示：“Spring，请在创建这个类时帮我自动注入所有必要的参数。”
    // 两种@Autowire
        // -> add on private field
        // -> add on constructor
    @Autowired
    public RegisterService(UserRepository userRepository, AuthorityRepository authorityRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.authorityRepository = authorityRepository;
        this.passwordEncoder = passwordEncoder;
    }
    // @Transactional —— 一次成功或全部失败
    // add() 方法做两件事：
        // 1. 保存用户
        // 2. 保存用户的权限
    // 这就是 @Transactional 做的事：
    // 保证数据库的两个操作要么都成功，要么都失败。
    // 否则数据库会乱掉（一个有用户，一个没权限）。

    /**
     *  @Transactional(isolation = Isolation.SERIALIZABLE)
     *  public void add(User user, UserRole role){
     *      userRepository.save(user);
     *      authorityRepository.save(new Authority(user.getUsername(), role.name()));
     *  }
     */
   // isolation = SERIALIZABLE: 最严格的隔离级别。处理注册这种敏感操作时，保证数据一致性最高。

    // add() 功能总结
    //    1. 把 User 写入数据库
    //    2. 再把这个 User 对应的 Role 写入 authority 表
    //    3. 两件事必须都成功（通过 @Transactional 保证）

    // role.name: 方法来自 Java 的 Enum 类，java自带
    //    role.name() → 返回 enum 的名字（永远不会被 override） -> 把enum常量变成一个string
    //    role.toString() → 可以被重写，不一定可靠

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void add(User user, UserRole role) throws UserAlreadyExistException{
        // -> use UserAlreadyExistException exception 做 user是否存在的check
        if (userRepository.existsById(user.getUsername())){
            throw new UserAlreadyExistException("User already exist");
        }
        // -> use the passwordEncoder to do the encryption.
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(true);
        // 🎯 这个 enabled -> Spring Security 或你自己的程序可以根据这个字段判断：
            // -> 用户是否被允许登录
            // -> 用户是否被管理员禁用
            // ->注册时默认启用（true）
            // -> 封号时可以改成 false

        // 🔍 那为什么注册时要写 user.setEnabled(true);？
            // -> 初次注册时，你必须把账号设为可用
            // -> 否则用户一注册完就被“禁用”，无法登录

        userRepository.save(user);
        authorityRepository.save(new Authority(user.getUsername(), role.name()));
    }
}

/**
 * RegisterService = 注册服务
 * 作用：
 * 👉 把用户存进数据库
 * 👉 给这个用户分配一个角色（比如 USER / ADMIN）
 */

//✅ 一句话确认你的理解
//
//✅ User： 是前端传来的数据，Spring 在 Controller 这里已经帮你 new 好并填好属性，所以在 Service 里直接用 user 就行，不需要再 new User(...)。
//        ✅ Role（UserRole.ROLE_GUEST）： 是后端自己在 Controller 里“决定”的，不是前端传来的。
//        ✅ Authority： 是你为了把“这个用户有什么角色”写进数据库而在 Service 里自己 new 出来的对象，因此需要 new Authority(...)。
//你的这句话可以这样改成更精确的版本：
//
//        ✅ 因为 User 对象是由前端传来的 JSON + Spring 帮忙组装好的，所以 Controller 调用 Service 时，User 已经是一个“现成的对象”，Service 直接拿来 save。
//        ✅ 因为 角色是后端根据业务规则决定的，而且前端并不会直接传 Authority 这张表的数据，所以 在 Service 里我们需要自己 new Authority(...)，再 save 进数据库。
//
//        🔍 再帮你拆成 3 个点记住：

//1. User 是“前端填表 + Spring 帮你 new 出来的”
//@PostMapping("/register")
//public void register(@RequestBody User user) {  // 这里的 user 已经是 new 好的
//    registerService.add(user, UserRole.ROLE_GUEST);
//}

//前端发 JSON
//Spring 根据 JSON 自动 new User() + 调用 setUsername()、setPassword()、setEnabled() 等
//
//所以到了 Controller 时，user 已经是一个完整对象
//Service 里自然就直接：userRepository.save(user);
//👉 你不需要再 new User，因为已经有人帮你 new 好了。

//2. Role 是后端“拍板决定”的，不相信前端
//registerService.add(user, UserRole.ROLE_GUEST);
//这里 UserRole.ROLE_GUEST 是你自己在 Controller 里写死的
//表示：所有新注册用户默认是 GUEST（普通用户）
//这一步只是 业务规则上的“决定”，还没有落到数据库里。

//3. Authority 是“权限表里的一条记录”，必须你自己 new
//在 Service 里：
//authorityRepository.save(
//    new Authority(user.getUsername(), role.name())
//        );

//因为：
//前端不会直接给你一条 Authority 对象（那太不安全）
//数据库中 authorities 这张表需要一条新记录
//这条记录是：“某个 username 拥有某个 role”
//所以你得自己 new Authority(...) 然后 save

//👉 User 虽然也是 new 出来的，但那一步由 Spring 帮你做了；
//👉 Authority 没人帮你做，只能你在 Service 里自己 new。

//🎯 超简短总结合成版（可以背）
//
//User：
//前端传 → Spring 在 Controller 自动 new 好 → Service 直接 save(user)。
//
//Role：
//后端在 Controller 里用 UserRole.ROLE_GUEST 决定 → 作为参数传给 Service。
//
//Authority：
//数据库里需要一条“用户名 + 角色”的记录 → 只能在 Service 里自己 new Authority(...) 再 save。
