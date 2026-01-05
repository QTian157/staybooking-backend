package com.tq.staybooking.repository;

/**
 * 1. Go to com.tq.staybooking.repository package and create ReservationRepository.
 * 2. Add a couple of methods to support list by stay and list by guest functions.
 * 3. Go to com.tq.staybooking.exception package and create ReservationCollisionException.
 * 4. Since we support reservations now,
     * we need to check active reservations before deleting a stay.
     * Go to the ReservationRepository interface and add a new method.
 * 5. Go to com.tq.staybooking.exception package and create StayDeleteException.
 */

import com.tq.staybooking.model.Reservation;
import com.tq.staybooking.model.Stay;
import com.tq.staybooking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByGuest(User guest); // 一个 Guest 查看“我自己的所有预订”

    List<Reservation> findByStay(Stay stay); // 查看某个 stay 的所有 reservation

    Reservation findByIdAndGuest(Long id, User guest); // for deletion
    // 只查“这个 id 且属于当前用户”的 reservation
    // 为什么不能用：findById(id)
    // 因为那样会发生：
        // Reservation r = findById(id);
        // delete(r);
        // ❌ 任何 guest 只要知道 reservationId，就能删别人的订单

    List<Reservation> findByStayAndCheckoutDateAfter(Stay stay, LocalDate date);
    // 这个房源，有没有退房日期在今天之后的订单？
    // 这个房源，还有没有没结束的订单？

}

/**
 * 一、JpaRepository<Reservation, Long> 到底是什么意思？
     * 这是一个用来操作 Reservation 实体的仓库，
     * 它的主键（@Id）的类型是 Long
     * 1️⃣ 第一个泛型：Reservation
         * 告诉 Spring Data JPA：👉 你要操作的是哪一张“表 / 实体”
         * Spring 就知道：
             * save() → 往 reservation 表 insert / update
             * findAll() → select * from reservation
             * delete() → delete from reservation ...
     * 2️⃣ 第二个泛型：Long
         * 告诉 Spring Data JPA：👉 这个实体的主键类型是什么
 * 二、JpaRepository 默认已经给了你什么？
     * save(reservation)
     * findById(id)
     * findAll()
     * deleteById(id)
     * existsById(id)
     * count()
     * 但它不知道你的“业务语义”
     * Spring 不知道：
         * 什么叫“guest 的所有 reservation”
         * 什么叫“某个 stay 的 reservation”
         * 什么叫“只能删自己的 reservation”
     * 👉 所以这些必须你自己定义
 */