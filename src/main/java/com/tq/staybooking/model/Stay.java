package com.tq.staybooking.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
//import tools.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;


import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * 1. Open your staybooking project in Intellij and go to the com.tq.staybooking.model package. Create a new class named Stay.
 * 2. Add some private fields to the Stay class. As you can see in the IDE, the StayReservedDate class is not available.
 * 3. Add builder() class
 * 4. Add a few Jackson related annotations.
 * 5. A few explanations for the annotation:
     * @JsonDeserialize makes sure the Jackson library will use the Builder class to convert JSON format data to the Stay object.
     * @JsonProperty makes sure to map guestNumber field to the guest_number key in JSON format data.
     * @JsonIgnore makes sure we don’t return reserved date information when returning the stay information in JSON format because,
     * in our design, the front end doesn’t need to show the details about a stay’s reserved dates.
 * 6. At last, annotate the Stay class with Hibernate-related annotations.
 * 7. Go to the com.tq.staybooking.repository package and create a new class StayRepository.
 * 8. Go back to the Stay class to add a list of StayImage as a private field.
 * 9. Create Image Upload Service:Open the application.properties file and add a new variable named gcs.bucket. Remember to use your bucket name as the value.
 */

@Entity
@Table(name = "stay")
@JsonDeserialize(builder = Stay.Builder.class)
public class Stay implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;     // 对应stay.id
    private String name; // 房源名字
    private String description;
    private String address;

    @JsonProperty("guest_number")
    private int guestNumber;   // 对应stay.guest_number

    @ManyToOne
    @JoinColumn(name = "user_id")
    // @JoinColumn(name = "...") 里的 name 👉 指的是「当前这张表中的数据库列名（外键列）」不是 Java 字段名，不是 @MapsId 的那个字段名。
    // @JoinColumn(name = "user_id") 的意思就是 在 stay 表中有一列叫 user_id，这列作为外键，存的是 users.username 的值(users表中的主键)。
    private User host;   // 外毽 -> user表

    @JsonIgnore
    @OneToMany(mappedBy = "stay", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // mappedBy = "stay" -> 对方持有外键的 Java 字段名
        // “别在我这边建外键
        //外键在对方那个字段（叫 stay）那里”
    private List<StayReservedDate> reservedDates;  //和 stay_reserved_date表对应

    @OneToMany(mappedBy ="stay", cascade = CascadeType.ALL, fetch=FetchType.EAGER)
    private List<StayImage> images;

    public Stay() {}
    private Stay(Builder builder){
        this.id = builder.id;
        this.name = builder.name;
        this.description = builder.description;
        this.address = builder.address;
        this.guestNumber = builder.guestNumber;
        this.host = builder.host;
        this.reservedDates = builder.reservedDates;

        this.images = builder.images;
    }
    public Long getId(){
        return id;
    }
    public String getName(){
        return name;
    }
    public String getDescription(){
        return description;
    }
    public String getAddress(){
        return address;
    }
    public int getGuestNumber(){
        return guestNumber;
    }
    public User getHost(){
        return host;
    }
    public List<StayReservedDate> getReservedDates(){
        return reservedDates;
    }

    public List<StayImage> getImages(){
        return images;
    }

    public Stay setImages(List<StayImage> images){
        this.images = images;
        return this;
    }


    public static class Builder{
        @JsonProperty("id")
        private Long id;     // 对应stay.id

        @JsonProperty("name")
        private String name;

        @JsonProperty("description")
        private String description;

        @JsonProperty("address")
        private String address;

        @JsonProperty("guestNumber")
        private int guestNumber;   // 对应stay.guest_number

        @JsonProperty("host")
        private User host;   // 外毽 -> user表

        @JsonProperty("dates") // 👉 前端传进来的 JSON key 就必须是 "dates"
        private List<StayReservedDate> reservedDates;  //和 stay_reserved_date表对应

        @JsonProperty("images")
        private List<StayImage> images;

        public Builder setId(Long id){
            this.id = id;
            return this;
        }
        public Builder setName(String name){
            this.name = name;
            return this;
        }
        public Builder setDescription(String description){
            this.description = description;
            return this;
        }
        public Builder setAddress(String address){
            this.address = address;
            return this;
        }
        public Builder setGuestNumber(int guestNumber){
            this.guestNumber = guestNumber;
            return this;
        }
        public Builder setHost(User host){
            this.host = host;
            return this;
        }

        public Builder setReservedDates(List<StayReservedDate>  reservedDates) {
            this.reservedDates = reservedDates;
            return this;
        }


        public Builder setImages(List<StayImage> images){
            this.images = images;
            return this;
        }


        public Stay build(){
            return new Stay(this);
        }
        // 调用builder（） -> 调用Stay有参constructor：
        //        private Stay(Builder builder){
        //            this.id = builder.id;
        //            this.name = builder.name;
        //            this.description = builder.description;
        //            this.address = builder.address;
        //            this.guestNumber = builder.guestNumber;
        //            this.host = builder.host;
        //            this.reservedDates = builder.reservedDates;
        //        }
    }

    public void setHost(User host) {
        this.host = host;
    }
}

/**
 * Stay = 房源，StayReservedDate = 这套房每天的日历，StayReservedDateKey = 这张日历表的复合主键。
 * 一、先别管代码：先想业务故事
 * 想象你在做一个小型 Airbnb：
     * -> User：房东／用户
     * -> Stay：一套房源（某个 house / apartment）
     * -> StayReservedDate：这套房哪一天已经被别人订走了（被占用的日期）
     * 一个房源会有很多已经被订掉的日期，比如
     * | stay_id | date       |
     * | ------- | ---------- |
     * | 1       | 2025-12-20 |
     * | 1       | 2025-12-21 |
     * | 2       | 2025-12-25 |
     * 这些“房源 + 日期”的组合，就需要一张单独的表来存，这就是 stay_reserved_date 表，对应的 Java 类就是 StayReservedDate。
 * 为什么要单独一张“日期表”？
     * 因为关系型数据库（MySQL）是一行一行存的：
     * 不能很方便地在一行里面存 “2025-12-20, 2025-12-21, 2025-12-22...” 这样的列表，然后又能拿出来做日期比较、查找冲突。
     * 最标准做法：一行代表一个日期。
     * stay 表：存房源基本信息（名字、地址、几个人能住、房东是谁…）
     * stay_reserved_date 表：存某个房源在某一天已经被订走的信息
 * 二、从数据库角度看这三个类是谁
 * 1. stay 表（对应 Stay 类）
     * | 列名           | 类型      | 含义                   |
     * | ------------  | -------  | ------------------     |
     * | id            | BIGINT   | 主键（房源ID）           |
     * | name          | VARCHAR  | 房源名                  |
     * | description   | VARCHAR  | 描述                   |
     * | address       | VARCHAR  | 地址                   |
     * | guest_number  | INT      | 最大入住人数             |
     * | user_id       | BIGINT   | 房东ID → 对应 `User` 表 |
     * 👉 这里：一个 Stay（房源）有很多 StayReservedDate（被订掉的日期），所以是 一对多。
 * 2. stay_reserved_date 表（对应 StayReservedDate 类）
     * | 列名      | 类型     | 含义              |
     * | ------- | ------ | --------------- |
     * | stay_id | BIGINT | 哪个房源            |
     * | date    | DATE   | 哪一天被订走          |
     * | ...     | ...    | 可能还有其他列（比如 who） |
     * 设计上希望：同一个房源的同一天 只能出现一次，不能有重复，比如：✅ stay_id = 1, date = 2025-12-20,❌ 再插入一行 stay_id = 1, date = 2025-12-20（重复）
     * 所以它的“主键”就自然是：stay_id + date 这两个字段一起唯一 ->复合主键（composite primary key）
 * 三、为什么要有 StayReservedDateKey 这个类？
 * JPA/Hibernate 里，如果一个表的主键是“多个字段组合”，就不能只写一个 @Id Long id 解决，而是要：
     * -> 新建一个单独的 key 类 → StayReservedDateKey
     * -> 把主键里所有字段写到这个类里面（stay_id、date）
     * -> 在 StayReservedDate 里面用 @EmbeddedId 来引用这个 key
     * @EmbeddedID
     * 告诉 JPA：这个类不是一个表，而是“可以嵌入到别的实体里的一个复合键/复合字段”。
     * 告诉 JPA：这个字段就是这张表的主键，而且这个主键是一个“复合的对象”（刚才那个 StayReservedDateKey）。
 * 四、三个类之间的关系，用“图”来帮你记
 * 1. Stay：一套房源
     * 字段：id, name, description, address, guestNumber, host(房东), reservedDates(被订走的日期列表)
 * 2. StayReservedDate：“某套房在某一天被订走” 这件事
     * 主键字段都藏在一个 id: StayReservedDateKey
     * 同时有一个 stay: Stay 指回这套房源
 * 3. StayReservedDateKey：
     * 就是 (stay_id, date) 这个组合
     * 是 JPA 需要的“复合主键类”
 * 可以想象成：
     * Stay（id=1, name="Nice House"）
     *   ├── StayReservedDate(id: {stay_id=1, date=2025-12-20})
     *   ├── StayReservedDate(id: {stay_id=1, date=2025-12-21})
     *   └── StayReservedDate(id: {stay_id=1, date=2025-12-25})
 *   五、为什么 StayReservedDate 不写 setter？
 *   这里有个设计小细节（老师提到的）：
     * -> 这些“被订走的日期”都是从数据库里查出来的，我们不会在 Java 代码里频繁去修改它。
     * -> 新增预定 / 删除预定的时候，会通过 Repository、Service 来做，而不是在代码里随便 setXXX 修改。
     * -> 不提供 setter 能让这个对象更“只读”，避免乱改主键这种危险操作。
 * 六、回到 Stay：既是数据库实体，又是给前端用的 JSON 模型
 * Stay 同时扮演两个角色：
     * -> 数据库实体（Entity）：用来映射 stay 表
     * -> 接口返回的 JSON 对象：前端需要看这个对象
 * 1. 和数据库的关系：JPA/Hibernate 注解:
     * -> @Entity               // 这是一个数据库实体
     * -> @Table(name = "stay") // 对应 stay 表
     * -> @Id + @GeneratedValue：主键自增
     * -> @ManyToOne + @JoinColumn(name = "user_id")：多个房源（Stay）对应同一个房东（User）
     * -> @OneToMany(mappedBy = "stay")：一个房源有很多被订走的日期（StayReservedDate）
 * 2. 和 JSON 的关系：Jackson 注解:
     * @JsonDeserialize(builder = Stay.Builder.class):告诉 Jackson：当我从 JSON 转成 Stay 对象时，请用这个 Builder 来建对象。
     * @JsonProperty("guest_number"):
         * 解决“命名风格不一样”的问题：
             * Java 字段：guestNumber（驼峰）
             * JSON 字段：guest_number（下划线）
         * Jackson 看到这个注解，就知道 JSON 里的 guest_number 要填到 guestNumber 这个字段里。
     * @JsonIgnore 在 reservedDates 上 -> 表示：当我把 Stay 转成 JSON 返回给前端时，不要把 reservedDates 这个字段带上。
         * 因为前端页面通常只显示：房源名、地址、描述、最多住几个人、房东是谁. 而不会显示这一堆占用日期的细节（这些可能单独接口查）。
 */