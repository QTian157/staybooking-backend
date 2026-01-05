package com.tq.staybooking.service;

import com.tq.staybooking.model.Stay;
import com.tq.staybooking.repository.LocationRepository;
import com.tq.staybooking.repository.StayRepository;
import com.tq.staybooking.repository.StayReservationDateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 1. Go to com.tq.staybooking.service package and create the SearchService class.
 * 2. Add StayRepository, StayReservationDateRepository and LocationRepository as the private field
     * and create a constructor for initialization.
 * 3. Add the search method to use the repositories we just created.
 * 4. Go to the com.tq.staybooking.config package and create a new class named ElasticsearchConfig.
 */
@Service
public class SearchService {
    private StayRepository stayRepository;
    private LocationRepository locationRepository;
    private StayReservationDateRepository stayReservationDateRepository;

    @Autowired
    public SearchService(StayRepository stayRepository, StayReservationDateRepository stayReservationDateRepository, LocationRepository locationRepository){
        this.stayRepository = stayRepository;
        this.stayReservationDateRepository = stayReservationDateRepository;
        this.locationRepository = locationRepository;
    }

    public List<Stay> search(int guestNumber, LocalDate checkinDate, LocalDate checkoutDate, double lat, double lon, String distance){

        // 1) 基本校验（强烈建议）
        if (checkinDate == null || checkoutDate == null || !checkoutDate.isAfter(checkinDate)) {
            return Collections.emptyList();
        }
        List<Stay> filteredStays = new ArrayList<>();

        // 2) ES：按距离找候选 stayIds
        List<Long> stayIds = locationRepository.searchByDistance(lat, lon, distance);
        // ❓ 既然在 CustomLocationRepositoryImpl.searchByDistance() 里已经处理过了
        // ❓ 这里是不是多余？
        // -> Impl 里做的“判断”是什么性质？ -> “保证这个 Repository 方法本身健壮” -> 输入参数合法/ 方法本身能安全执行
        // -> 那 Service 层的判断是在干嘛？（这是关键）-> “如果第一阶段搜索结果为空，整个搜索流程可以提前结束”
        if (stayIds == null || stayIds.isEmpty()){
            return filteredStays;
        }

        // 3) DB：找在日期区间内已被预订的 stayIds（注意 checkout 不包含）
            // -> 入住日（check-in）包含，退房日（check-out）不包含
            // -> [checkin, checkout) —— 左闭右开区间 -> checkoutDate.minusDays(1)
        Set<Long> reservedStayIds = stayReservationDateRepository.findByIdInAndDateBetween(stayIds, checkinDate, checkoutDate.minusDays(1));

        // 4) Service：剔除已被订的
        List<Long> filteredStayIds = new ArrayList<>();
        for (Long stayId : stayIds) {
            if (!reservedStayIds.contains(stayId)) {
                filteredStayIds.add(stayId);
            }
            if (filteredStayIds.isEmpty()) {
                return Collections.emptyList();
            }
        }

        // 5) DB：按 guestNumber 过滤并返回最终 Stay
        filteredStays = stayRepository.findByIdInAndGuestNumberGreaterThanEqual(filteredStayIds, guestNumber);
        return filteredStays;
    }
}
