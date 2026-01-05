package com.tq.staybooking.controller;

/**
 * 1. Go to com.tq.staybooking.controller package and create a new ReservationController class.
 * 2. Implement the reservation related APIs including list, add and delete.
 * 3. Go to StayController and add a new API to support list reservation by stay function.
 */

import com.tq.staybooking.exception.InvalidReservationDateException;
import com.tq.staybooking.model.Reservation;
import com.tq.staybooking.model.User;
import com.tq.staybooking.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
public class ReservationController {

    private ReservationService reservationService;

    @Autowired
    public  ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping( value = "/reservations")
    public List<Reservation> listReservation(Authentication authentication) {
        return reservationService.listByGuest(authentication.getName());
    }

    @PostMapping("/reservations")
    public void addReservation(@RequestBody Reservation reservation, Authentication authentication) {
        LocalDate checkinDate = reservation.getCheckinDate();
        LocalDate checkoutDate = reservation.getCheckoutDate();

        if(checkinDate.equals(checkoutDate)|| checkinDate.isAfter(checkoutDate) || checkinDate.isBefore(LocalDate.now())) {
            throw new InvalidReservationDateException("Invalid date for reservation");
        }
        reservation.setGuest(new User.Builder().setUsername(authentication.getName()).build());
        reservationService.add(reservation);
    }

    @DeleteMapping("/reservations/{reservationId}")
    public void deleteReservation(@PathVariable Long reservationId, Authentication authentication) {
        reservationService.delete(reservationId, authentication.getName());
    }
}
/**
 * 👉 add 的时候要 new User(...)，是因为你在“构造一个 Reservation 对象”
 * 👉 delete 的时候直接用 authentication.getName()，是因为你只是在“传一个用户名作为查询条件”
 * 本质区别：一个是在“造对象”，一个是在“用条件”。
 *
 * addReservation：你在干什么？
 * 👉 你在做的是：把前端传来的 JSON，变成一个完整、合法的 Reservation 对象，然后保存。
     * Controller 的职责之一是：
         * 把前端输入 → 变成一个合法的业务对象
         * 所以在 add 里，Controller 要负责：
         * 校验日期
         * 强制设置 guest
         * 然后把“完整 Reservation”交给 Service
 *
 * deleteReservation：你在干什么？
 * 👉 你在做的是：告诉 Service：删除“这个 id、而且属于当前用户”的 reservation。
     * 只是：
     * “请删除这个 id 的 reservation，但只能删当前用户的。”
     * Service 才是那个：
         * 组装查询条件
         * 查 DB
         * 做安全校验
         * 真正执行删除
 */