package com.tq.staybooking.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 1. Open your project in Intellij, go to the com.tq.staybooking.model package and create a new class called Reservation.
 * 2. Add some private fields and the corresponding getters/setters/builder pattern. Remember to add JSON-related annotations.
 * 3. Add Jackson and JPA related annotations.
 * 4. Go to com.tq.staybooking.repository package and create ReservationRepository.
 */

@Entity
@Table(name ="reservation")

@JsonDeserialize(builder = Reservation.Builder.class)
public class Reservation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonProperty("checkin_date")
    private LocalDate checkinDate;
    @JsonProperty("checkout_date")
    private LocalDate checkoutDate;

    @JsonProperty("guest")
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User guest;

    @ManyToOne
    @JoinColumn(name ="stay_id")
    private Stay stay;

    public Reservation(){};

    private Reservation(Builder builder){
        this.id = builder.id;
        this.checkinDate = builder.checkinDate;
        this.checkoutDate = builder.checkoutDate;
        this.guest = builder.guest;
        this.stay = builder.stay;
    }

    public Long getId(){
        return id;
    }
    public LocalDate getCheckinDate(){
        return checkinDate;
    }
    public LocalDate getCheckoutDate(){
        return checkoutDate;
    }
    public User getGuest(){
        return guest;
    }
    public Reservation setGuest(User guest){
        this.guest = guest;
        return this;
    }
    public Stay getStay(){
        return stay;
    }

    public static class Builder{
        @JsonProperty("id")
        private Long id;
        @JsonProperty("checkin_date")
        private LocalDate checkinDate;
        @JsonProperty("checkout_date")
        private LocalDate checkoutDate;
        @JsonProperty("guest")
        private User guest;
        @JsonProperty("stay")
        private Stay stay;

        public Builder setId(Long id){
            this.id = id;
            return this;
        }

        public Builder setCheckinDate(LocalDate checkinDate){
            this.checkinDate = checkinDate;
            return this;
        }

        public Builder setCheckoutDate(LocalDate checkoutDate){
            this.checkoutDate = checkoutDate;
            return this;
        }

        public Builder setGuest(User guest){
            this.guest = guest;
            return this;
        }

        public Builder setStay(Stay stay){
            this.stay = stay;
            return this;
        }

        public Reservation build(){
            return new Reservation(this);
        }
    }

}
/**
 * 问题 1. 为什么 Reservation(Builder builder) 里不直接写 this.builder = builder;
 * 👉 因为 builder 只是“造对象的工具”，不是对象本身的一部分。Reservation 不应该“记住 builder”。
     * Builder  ---->  Reservation
     * (临时工具)        (真正业务对象)
     * Builder 的作用是: 帮你一步一步收集参数，最后一次性生成一个完整的 Reservation -> 一旦 build() 完成，Builder 的使命就结束了。
 * 👉 如果你写成 this.builder = builder 会发生什么？
     * 那意味着：
         * Reservation
         *  ├── id
         *  ├── checkinDate
         *  ├── checkoutDate
         *  ├── guest
         *  ├── stay
         *  └── builder   ❌（多余 & 危险）
 * 👉 正确的 Builder 模式核心思想: Builder → 拷贝数据 → 生成不可依赖 Builder 的对象
 * 问题 2. 为什么外部的 setter 只有 setGuest() 一个？ 不给 setter ≠ 不能改，而是“不允许随便改”
 * 👉 因为 Reservation 大多数字段一旦创建就不应该再被随意改
 * 👉 guest 是一个受控、必须由系统强制设置的字段
 * 👉 我们先把 Reservation 的字段分 3 类
     * 🟦 1. 创建时必须确定、之后不允许乱改的
         * checkinDate
         * checkoutDate
         * stay
     * 这些字段一旦变了，等于换了一次预订：
         * 改日期 → 要重新检查冲突
         * 改 stay → 完全是另一笔订单
     * 所以：
         * 不提供 setter
         * 只能在 build 阶段一次性定好
     * 🟨 2. 系统控制字段
         * guest
     * 这个字段有特殊业务含义：
         * 不能相信前端传的 guest
         * 必须从 Principal / JWT 中取当前用户
     * 所以 Controller 里是这样：
         * reservation.setGuest(
         *     new User.Builder().setUsername(principal.getName()).build()
         * );
     * 这不是“普通 setter”，而是 系统强制修正
     * 🟥 3. id
         * id
     * id 是数据库生成的
         * 业务代码不应该 setId
     * 所以外部没有 setId
 * 问题 3. 为什么 Builder 里有 setter，却没有 getter？
 * 👉 因为 Builder 是“写入工具”，不是“读取对象”
     * Builder 的唯一职责: 接收参数 → 存起来 → build()
     * 你从来不会这样用 Builder：你从来不会这样用 Builder：
     * 因为：
         * Builder 不是业务对象
         * 你也不会把 Builder 传来传去
 * 👉 Builder 是“单向数据流”
     * 调用方
     *   ↓ setXxx(...)
     * Builder
     *   ↓ build()
     * Reservation
 * Builder 和 Reservation 的角色对比（很重要）
     * | 类           | 目的   | 有 getter | 有 setter |
     * | ----------- | ---- | -------- | -------- |
     * | Reservation | 业务实体 | ✅        | ❌（只给必要的） |
     * | Builder     | 构建工具 | ❌        | ✅        |
 *
 */

/**
 * 用「前端 JSON → Java 对象 → 数据库表」这条完整链路来解释
     * @JsonProperty   👉  给 JSON / 前端用的
     * @ManyToOne      👉  给 Java 对象关系 / ORM 用的
     * @JoinColumn     👉  给 数据库表结构 / 外键列 用的
 * 1️⃣ @JsonProperty —— 给「前端 JSON」用的 ✔️
 * 👉 它就是 JSON ↔ Java 的字段映射
     * @JsonProperty("guest")
     * private User guest;
     * 意思是：
         * 前端 / Postman 里用的是："guest"
         * Java 里字段名是：guest
         * Jackson 负责在两者之间转换
 * 2️⃣ @ManyToOne —— 给「Java 对象关系 / ORM」用的 ✔️
     * 这是 JPA / Hibernate 的世界。
         * @ManyToOne
         * private User guest;
     * 多个 Reservation → 对应一个 User
         * User 1
         *  ├── Reservation A
         *  ├── Reservation B
         *  └── Reservation C
 *  3️⃣ @JoinColumn(name = "user_id") —— 给「数据库列名」用的 ✔️
     *  @JoinColumn(name = "user_id")
     *  👉 name 就是数据库表里的列名
     *  如果你不写 @JoinColumn 会怎样？
         * Hibernate 会自动生成一个名字，通常是：guest_id or guest_username
 * 4️⃣ 三个注解放在一起，发生了什么？（最重要）
     * @JsonProperty("guest") // 给前端看的
     * @ManyToOne // 给 ORM / Java 看的
     * @JoinColumn(name="user_id") // 给数据库看的
     * private User guest;
     * 👉 同一个字段，被 3 个“系统”同时使用：
         * | 系统         | 看这个字段时理解成                 |
         * | ---------- | ------------------------- |
         * | 前端 / JSON  | `"guest": {...}`          |
         * | Java / JPA | Reservation → User 的多对一关系 |
         * | 数据库        | reservation.user_id 外键    |
 */

/**
 * 一、Jackson 到底是干嘛的？（一句话版）
 * 👉 Jackson = JSON ↔ Java 对象 的翻译官
     * 前端发 JSON
     * Spring MVC 把 JSON 交给 Jackson
 * Jackson 负责：
     * JSON → Java 对象
     * Java 对象 → JSON
 * 默认规则是：
     * {
     *   "checkinDate": "2025-01-01"
     * }
     * ↔ reservation.setCheckinDate(...)
 * 二、最基础模式（你以前一定见过）
     * 1️⃣ 没 Builder、没注解
         * public class Reservation {
         *     private LocalDate checkinDate;
         *
         *     public void setCheckinDate(LocalDate checkinDate) {
         *         this.checkinDate = checkinDate;
         *     }
         * }
     * JSON：
         * {
         *   "checkinDate": "2025-01-01"
         * }
     * ✔ Jackson 自动找：
         * 字段名 / setter 名
         * 类型匹配就行
         * 👉 这是“Java Bean 模式”
 * 三、字段名不一样怎么办？@JsonProperty
     * 你现在用的第一个 Jackson 注解：
         * @JsonProperty("checkin_date")
         * private LocalDate checkinDate;
     * 意思是：
         * JSON 里叫 checkin_date
         * Java 里叫 checkinDate
     * 所以：
         * { "checkin_date": "2025-01-01" }
         * ✔ 能正确进来
 * 四、⚠️ 为什么你不能再用普通模式了？
     * 因为你做了这件事 👇
     * @JsonDeserialize(builder = Reservation.Builder.class)
     * 这句话直接改变了 Jackson 的工作方式。
     * 从这一刻起：
     * ❌ Jackson 不再：
         * new Reservation();
         * reservation.setCheckinDate(...)
     * ✅ 而是：
         * Reservation.Builder b = new Reservation.Builder();
         * b.setCheckinDate(...)
         * b.setStay(...)
         * Reservation r = b.build();
 * 五、Builder 模式下，Jackson 到底看谁？
     * ⚠️ 这是很多人最容易混乱的地方。
     * 👉 Jackson 只看 Builder，不看 Entity 本身
 * 所以：
     * @JsonProperty("guest")
     * @ManyToOne
     * private User guest;
 * 👉 这个 @JsonProperty 对 Jackson 已经没意义了
 * 真正生效的是👇
     * public static class Builder {
     *     @JsonProperty("guest")
     *     private User guest;
     * }
 */