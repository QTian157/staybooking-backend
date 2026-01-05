package com.tq.staybooking.model;

import javax.persistence.*;

import java.io.Serializable;

/**
 * 1. Under the same com.tq.staybooking.model package, create the StayReservedDate class.
 * 2. For the three columns in the StayReservedDate table,
     * we choose to create the composite primary key based on stay_id and date.
     * So create another class called StayReservedDateKey under the com.tq.staybooking.model package.
 * 3. Public setters are not added since we won’t need them in our code.
     * All the stay reserved date information is read from the database,
     * so we don’t need to update them in the Java code.
 * 4. Next, annotate the class and private field to make it supported by Hibernate.
 * 5. Finally, go back to the Stay class and add necessary getters, setters, and constructors.
 */

@Entity
@Table(name = "stay_reserved_date")
public class StayReservedDate implements Serializable {
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private StayReservedDateKey id;

    @MapsId("stayId")
    // @MapsId -> 当「外键 = 主键的一部分」时才用
    // column stay_id also is a forenign key of table stay, stay_id 既是外键，又是主键的一部分
    // @MapsId("xxx") 里的 "xxx" 👉 必须等于 @EmbeddedId 里那个类的「Java 字段名」
    @ManyToOne
    @JoinColumn(name = "stay_id")
    // @JoinColumn(name = "...") 里的 name 👉 指的是「当前这张表中的数据库列名（外键列）」不是 Java 字段名，不是 @MapsId 的那个字段名。
    private Stay stay;

    public StayReservedDate(){};
    public StayReservedDate(StayReservedDateKey id, Stay stay){
        this.id = id;
        this.stay = stay;
    }

    public StayReservedDateKey getId(){
        return id;
    };

    public Stay getStay(){
        return stay;
    }

}
/**
 * 5️⃣ 一张“永不过期”的判断表（你以后照这个做就不会错）
     * 看到数据库结构，立刻问自己三句话：
     * Q1：外键在哪张表？
         * → 那张表：@ManyToOne
     * Q2：另一边我想不想用 List 访问？
         * → 想：@OneToMany(mappedBy=...)
     * Q3：外键是不是主键的一部分？
         * → 是：@MapsId
 * 6️⃣ 用一句“人话”帮你彻底定型
     * ManyToOne 是“我属于谁”
     * OneToMany 是“我有多少个”
     * mappedBy 是“外键不在我这”
     * MapsId 是“外键同时也是主键”
 */
