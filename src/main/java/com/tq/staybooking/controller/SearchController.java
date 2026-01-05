package com.tq.staybooking.controller;

import com.tq.staybooking.exception.InvalidSearchDateException;
import com.tq.staybooking.model.Stay;
import com.tq.staybooking.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 1.Go to com.tq.staybooking.controller package and create the SearchController class.
 * 2. Implement the searchStays method.
 */

@RestController
public class SearchController {
    private SearchService searchService;

    @Autowired
    public SearchController(SearchService searchService){
        this.searchService = searchService;
    }

    @GetMapping(value = "/search")
    public List<Stay> searchStays(
            @RequestParam(name = "guest_number") int guestNumber,
            @RequestParam(name = "checkin_date") String start,
            @RequestParam(name = "checkout_date") String end,
            @RequestParam(name = "lat") double lat,
            @RequestParam(name = "lon") double lon,
            @RequestParam(name = "distance", required = false) String distance){

        LocalDate checkinDate = LocalDate.parse(start, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDate checkoutDate = LocalDate.parse(end, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        if (checkinDate.equals(checkoutDate) || checkinDate.isAfter(checkoutDate) || checkinDate.isBefore(LocalDate.now())){
            throw new InvalidSearchDateException("Invalid date for search");
        }
        return searchService.search(guestNumber, checkinDate, checkoutDate, lat, lon, distance);
    }
}
/**
 * 一。 那 /search 为什么不需要拿 Authentication？
 * 因为你现在的 search 是“公开的 guest 搜索”，结果不是“只属于某个 guest 的数据”。
     * search 需要的输入是：
         * lat/lon/distance
         * 日期
         * 人数
         * 这些跟“你是谁”无关（至少你目前的需求是这样）。
     * 当然，如果你以后要：
         * 记录搜索历史
         * 个性化推荐
         * 会员等级不同返回不同内容
         * 那 /search 也会开始用 Authentication。
 * 二。 那为什么 stay Controller 还要拿 Authentication？
 * 你在 /stays 这里做了两件不同的事：
     * 权限控制：只有 HOST 能访问
     * 业务过滤：访问了以后，只返回“这个 host 自己的 stays”
 * 三。 权限控制在哪里做？
     * 一般在securityConfig里面做，在进入controller之前就已经确认身份了。
     * 如果需要这个身份的摸个id做什么才需要在controller里做authentication
     * @PreAuthorize
 */