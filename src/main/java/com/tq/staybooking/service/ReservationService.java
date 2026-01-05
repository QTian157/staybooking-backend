package com.tq.staybooking.service;

import com.tq.staybooking.exception.ReservationCollisionException;
import com.tq.staybooking.exception.ReservationNotFoundException;
import com.tq.staybooking.model.*;
import com.tq.staybooking.repository.ReservationRepository;
import com.tq.staybooking.repository.StayReservationDateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * 1. Go to com.tq.staybooking.service package and create ReservationService.
 * 2. Add ReservationRepository and StayReservationDateRepository as the private field.
 * 3. Implement list, save and delete related functions.
 * 4. Since we support reservations now,
     * we need to check active reservations before deleting a stay.
     * Go to the ReservationRepository interface and add a new method.
 */
@Service
public class ReservationService {

    private ReservationRepository reservationRepository;
    private StayReservationDateRepository stayReservationDateRepository;

    @Autowired
    public ReservationService(ReservationRepository reservationRepository, StayReservationDateRepository stayReservationDateRepository){
        this.reservationRepository = reservationRepository;
        this.stayReservationDateRepository = stayReservationDateRepository;
    }

    public List<Reservation> listByGuest(String username){
        return reservationRepository.findByGuest(new User.Builder().setUsername(username).build());
    }

    public List<Reservation> listByStay(Long stayId){
        return reservationRepository.findByStay(new Stay.Builder().setId(stayId).build());
    }

    // 这段代码的目标是什么？
    // 给某个 Stay（房源）在一段日期内创建一个 Reservation（预订），并确保不和别人冲突。
    // 关键点：不能双订（同一个房源同一天只能有一个客人住）。
    // 为了解决“不能双订”，系统用了两张表/两类数据：
    // Reservation：记录这笔订单（checkin、checkout、guest、stay）
    // StayReservedDate：记录这个 stay 的“每天是否被占用”（按天拆开）
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void add(Reservation reservation) throws ReservationCollisionException {
        // 1) 先查：这段日期有没有被占用
        Set<Long> stayIds = stayReservationDateRepository.findByIdInAndDateBetween(
                Arrays.asList(reservation.getStay().getId()),
                reservation.getCheckinDate(),
                reservation.getCheckoutDate().minusDays(1));

        // 2) 如果有冲突：直接拒绝
        if (!stayIds.isEmpty()) {
            throw new ReservationCollisionException("Duplicate reservation");
        }
        // stayReservationDateRepository.findByIdInAndDateBetween() -> “在我想订的这段日期里，有没有已经被占用的 stay？”
        // 如果 没有任何一天被占用
        //→ 数据库查不到记录
        //→ 返回 空集合
        //如果 哪怕有一天被占用
        //→ 数据库能查到至少 1 条记录
        //→ 返回 非空集合（里面有 stayId）

        // 3) 如果没冲突：生成每一天的占用记录
        List<StayReservedDate> reservedDates = new ArrayList<>();

        for (LocalDate date = reservation.getCheckinDate();
             date.isBefore(reservation.getCheckoutDate());
             date = date.plusDays(1))
        {
            reservedDates.add(
                    new StayReservedDate(
                            new StayReservedDateKey(reservation.getStay().getId(), date),
                            reservation.getStay()));
            // 每一条 StayReservedDate 的主键通常是复合主键：(stay_id, date)
        }
        // 4) 批量写入“占用日期表”
        // 把刚才生成的每天占用写进数据库。
        //为什么先写这个？因为它是“防冲突的锁”。
        //如果先写 Reservation，再写日期，出错时会留下脏数据更麻烦。
        stayReservationDateRepository.saveAll(reservedDates);
        // 5) 再写入 Reservation 本体（订单记录）
        // ✅ 先占坑（每天） → ✅ 再记订单（区间）
        reservationRepository.save(reservation);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void delete(Long reservationId, String username) throws ReservationNotFoundException {
        Reservation reservation = reservationRepository.findByIdAndGuest(reservationId, new User.Builder().setUsername(username).build());

        if (reservation == null) {
            throw new ReservationNotFoundException("Reservation is not available");
        }

        // 这里的stayReservationDateRepository.deleteById(id)是默认function
        // 这里的id是复合id (stay_id, date)
        for (
                LocalDate date = reservation.getCheckinDate();
                date.isBefore(reservation.getCheckoutDate());
                date = date.plusDays(1))
        {
            stayReservationDateRepository.deleteById(
                    new StayReservedDateKey(reservation.getStay().getId(), date));
        }
        reservationRepository.deleteById(reservationId);

    }

}

