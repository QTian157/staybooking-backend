package com.tq.staybooking.model;

import javax.persistence.Column;

import javax.persistence.Embeddable;


import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * 1. For the three columns in the StayReservedDate table,
     * we choose to create the composite primary key based on stay_id and date.
     * So create another class called StayReservedDateKey under the com.tq.staybooking.model package.
 * 2. Add the public getters, setters, and constructors to StayReservedDateKey class.
 * 3. Since we’re going to use StayReservedDateKey as the primary key of the StayReservedDate table,
     * we need to mark it as @Embeddable,
     * add a serialVersionUID and
     * implement both hashCode() and equals().
 * 4. Go back to the StayReservedDate class, and finish the code
 */

// @Embeddable:
// 告诉 JPA：这个类不是一个表，而是“可以嵌入到别的实体里的一个复合键/复合字段”。
// 告诉 JPA：这个字段就是这张表的主键，而且这个主键是一个“复合的对象”（刚才那个 StayReservedDateKey）。
@Embeddable
public class StayReservedDateKey implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "stay_id")
    private Long stayId;
    @Column(name = "date")
    private LocalDate date;

    public StayReservedDateKey(){};
    // JPA Spec 明确要求：
    //The embeddable class must have a public or protected no-arg constructor.

    public StayReservedDateKey(Long stayId, LocalDate date){
        this.stayId = stayId;
        this.date = date;
    }
    public Long getStayId(){
        return stayId;
    }
    public StayReservedDateKey setStayId(Long stayId){
        this.stayId = stayId;
        return this;
    }

    public LocalDate getDate(){
        return date;
    }
    public StayReservedDateKey setDate(LocalDate date){
        this.date = date;
        return this;
    }

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StayReservedDateKey that = (StayReservedDateKey) o;
        return stayId.equals(that.stayId) && date.equals(that.date);

    }

    @Override
    public int hashCode(){
        return Objects.hash(stayId, date);
    }
}

