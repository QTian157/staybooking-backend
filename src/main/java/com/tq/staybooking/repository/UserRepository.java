package com.tq.staybooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
//因为：你写了：extends JpaRepository<User, String>
//这里的 User 必须告诉 Java 从哪里来的，否则 Java 不认识它，就会报红。
//这个必须有，要不然JpaRepository<User, String> User飘红


import javax.swing.*;
import com.tq.staybooking.model.User;
/**
 * Create an interface named UserRepository under the com.tq.staybooking.repository package.
 * As you can see, we created the UserRepository interface to extend the JpaRepository interface provided by Spring. There are several points here:
 * The type parameters for JpaRepository are User and String, the first one corresponding to the name of the model class, the second one corresponding to the ID type of the model class.  By default,  Spring Boot enables the JPA repository support and looks in the package (and its subpackages) where @SpringBootApplication is located.
 * By extending the JpaRepository, Spring can help provide some default implementations of common database operations. You can expose any of them and Spring will take care of the real implementation. The full list of default operations could be found at
 * https://docs.spring.io/spring-data/commons/docs/current/api/org/springframework/data/repository/CrudRepository.html
 *
 * Besides the default operations, you can also define custom operations. As long as you follow the naming convention provided by Spring, you can only define the method in your @Repository interface and Spring can help you with the real implementation:
 * https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods.query-creation
 *
 * https://www.baeldung.com/the-persistence-layer-with-spring-data-jpa
 */

@Repository
public interface UserRepository extends JpaRepository<User, String> {
}


//🧩 1）什么是 interface（接口）？
//Java 里：
//class = 具体的东西（可以 new）
//rface = 一份“规范/说明书”，不能 new，只能让别人实现
//Spring Data JPA 的 Repository 就是接口，不需要你写实现代码，Spring 自动帮你实现。

//🧩 2）为什么 Repository 要写在 repository 包下面？
//因为 Spring Boot 默认会自动扫描 跟 @SpringBootApplication 同级以及它以下的包。
//你的 Repository 放在：
//com.tq.staybooking.repository
//
//而你的主程序 StaybookingApplication 在：
//com.tq.staybooking
//所以 repository 作为它的子包，会自动被扫描到。

//🧩 3）@Repository 是什么？
//这是一个 Spring 的注解，让 Spring 知道：“这个接口是用来访问数据库的 Repository。”
//Spring 会把它加入到 IOC 容器中，让 Service 可以注入它（@Autowired）。

//🧩 4）extends JpaRepository<User, String> 是什么意思？
//这个非常重要！
//这是说：让 Spring Data JPA 帮你自动生成数据库操作方法。
//📌 JpaRepository<User, String> 两个泛型参数含义：
//        1️. User
//表示你操作的是 User 这个实体类（model 类）。
//        2️. String
//表示你的 User 的主键（id）的类型是 String。
//也就是说，Spring 会基于：
//User 表
//id 是字符串类型
//自动生成增删改查。

//🧩 5）为什么需要写接口，却不用写实现？
//因为 Spring Data JPA 会自动生成实现。
//你写：
//
//public interface UserRepository extends JpaRepository<User, String> {}
//不用写一个类去实现它，Spring 会给你：
//findAll()
//findById()
//save()
//delete()
//count()
//等等几十种 CRUD 功能。
//
//你直接可以在 Service 里这样用：
//
//@Autowired
//private UserRepository userRepository;
//
//userRepository.save(user);
//userRepository.findById("abc");
//
//不用你写 SQL，Spring 自动帮你写。


//🧩 6）你还可以写自定义方法（不用实现！Spring 自动实现）
//比如你加上：
//User findByUsername(String username);
//
//Spring 会根据方法名自动生成 SQL：
//SELECT * FROM user WHERE username = ?
//
//太神奇了，就是这么好用。

//📌 完整总结（最简单版本）
//        | 内容                                  | 意思                 |
//        | ----------------------------------- | ------------------ |
//        | interface                           | 数据库接口，不用写实现        |
//        | @Repository                         | 告诉 Spring 这是数据库访问层 |
//        | extends JpaRepository<User, String> | 自动获得 CRUD 方法       |
//        | import User                         | 必须的，不然找不到类         |
//        | 不用写实现类                              | Spring 自动生成数据库操作代码 |

//🧠 你只需要记住一句话
//UserRepository 是一个“数据库操作接口”，
//Spring Data JPA 会根据 User 和 id 类型，自动生成所有数据库方法，让你不用写 SQL。

//✅ 1. Overview（概览）
//文章主旨：
//Spring Data JPA = 一套把数据库访问简化到极致的工具。
//以前传统方式：
    //你要写 DAO 接口
    //自己写 DAO 实现类
    //自己写 SQL 或 HQL
    //配置事务
//很麻烦。

//Spring Data JPA 出现后：
//你只写接口（Repository），Spring 自动帮你生成所有数据库代码。
//例如：
//public interface UserRepository extends JpaRepository<User, String> {}
//这一行就自动拥有：
    // save
    // delete
    // findAll
    // findById
    // update（自动通过 save 完成）
    // count
    // 等几十个方法。
//你不用写实现。

//✅ 2. No More DAO Implementations（不需要 DAO 实现类了）
//以前 DAO 要这样写（旧时代）：
//public class UserDaoImpl implements UserDao {
//    @Override
//    User findById(int id){
//        // 写SQL 或 HQL
//    }
//}

//Spring Data JPA 时代：
//public interface UserRepository extends JpaRepository<User, String> {}
//完事了 —— 自动帮你实现所有 CRUD。

//✅ 3. Custom Access Methods and Queries（自定义查询方法）
//如果默认的 CRUD 不够，你可以自己写方法，Spring 自动解析方法名并生成 SQL。
//比如：
//User findByName(String name);
//Spring 自动生成：
//SELECT * FROM user WHERE name = ?
//如果需要复杂查询，可以用：
    //@Query 注解
    //Specification 查询
    //Querydsl
    //Named Query（不常用）
//例如：
//@Query("SELECT u FROM User u WHERE u.age > :age")
//List<User> findUserOlderThan(@Param("age") int age);
//不用你写实现！

//✅ 4. Transaction Configuration（事务处理）
// Spring Data JPA 默认给你配置了事务：
    // 读操作：默认 @Transactional(readOnly = true)
    // 写操作：自动启用写事务
    // 你可以覆盖，但不写也能用。
//  🧩 4.1 Exception Translation（异常转换）
//  Spring 会自动把数据库异常转成 Spring 常见异常，比如：
//  DataIntegrityViolationException
//  DataAccessException
//  不用你处理底层 JPA 异常。

//✅ 5. Repository 配置（传统 Spring）
//如果你不是用 Spring Boot，需要手动启用：
//@EnableJpaRepositories(basePackages = "com.xxx.repository")
//Spring Boot 则自动启用。

//✅ 6. Java 或 XML 配置
//文章介绍：
//Spring 支持两种方式配置 JPA：
    //Java Config（现在主流）
    //XML Config（老项目用）
//Spring Boot 不需要你写繁琐配置。

//✅ 7. Maven Dependency（Maven 依赖）
//要使用 Spring Data JPA，需要：
//<dependency>
//    <groupId>org.springframework.data</groupId>
//    <artifactId>spring-data-jpa</artifactId>
//</dependency>

//Boot 直接：
//<artifactId>spring-boot-starter-data-jpa</artifactId>

//✅ 8. Using Spring Boot（Spring Boot 使用方式）
//如果用 Spring Boot：
//你只需要下两个依赖：
//spring-boot-starter-data-jpa
//h2（或 mysql）

//然后在 application.properties 写：
//spring.datasource.url = jdbc:mysql://...
//spring.datasource.username = root
//spring.datasource.password = xxx
//你的 JPA 就能直接工作，不需要繁琐配置。

//✅ 9. 工具推荐（IDE 工具）
// 文章告诉你：IDE（Eclipse、IntelliJ）有一些可视化工具，比如：
// 生成 ER 图
// 反向工程生成实体
// 查询调试工具（JPQL console）
// 并推荐 JPA Buddy 插件。

//⭐ 10. 结论（核心总结）
// 文章总结说：
// 使用 Spring Data JPA，你几乎不用写任何数据库访问代码。
// 你只写 Repository 接口，Spring 自动实现对数据库的所有操作。

//🎯 最终一句话总结（超级关键）
//Spring Data JPA = 只写接口，不写实现；自动生成 SQL；自动配置事务；自动转换异常；让数据库访问变得像调方法一样简单。