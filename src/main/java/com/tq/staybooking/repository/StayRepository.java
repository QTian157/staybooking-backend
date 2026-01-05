package com.tq.staybooking.repository;

import com.tq.staybooking.model.Stay;
import com.tq.staybooking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 1. Let’s check the backend diagram for stay management services.
     * As we mentioned before, common methods like save, deleteById, findById are defined in the JpaRepositry.
     * So we only need to define our method findByHost and findByIdAndHost.
 * 2.Go to the com.tq.staybooking.exception package and create a new exception class StayNotExistException.
 * 3. Go to the com.tq.service.controller package and create a new class StayController.
 * 4. Go to the StayRepository interface and add a new method called findByIdInAndGuestNumberGreaterThanEqual().
      * So besides location, the guest number is another parameter for search.
      * Can you think of some other search parameters we can support?
 * 5. Go to com.tq.staybooking.service package and create the SearchService class.
 */

@Repository
public interface StayRepository extends JpaRepository<Stay, Long> {
    List<Stay> findByHost(User user);
    Stay findByIdAndHost(Long id, User host);

    // 在给定的一堆 stay id 里，找出能容纳不少于 guestNumber 人的 stay，并把这些 stay 返回给我
    // 👉 重点：返回的是 stay 本身
    List<Stay> findByIdInAndGuestNumberGreaterThanEqual(List<Long> ids, int guestNumber);
    // ES 搜索（location） → 得到 stayIds
    //        ↓
    //MySQL 过滤（guestNumber） → 得到 Stay 实体
    //        ↓
    //Controller 返回给前端

    // 用一句话把这两个 Repository 方法对比钉死
    // | 方法                                         | 返回类型         | 原因             |
    // | ------------------------------------------ | ------------ | -------------- |
    // | `findByIdAndDateBetween`                   | `Set<Long>`  | 只做“是否可用”的过滤    |
    // | `findByIdInAndGuestNumberGreaterThanEqual` | `List<Stay>` | 要返回完整 stay 给前端 |

}

/**
 * 这两个方法不是为了“查得到”，
 * 而是为了：
     * 区分角色
     * 限制数据范围
     * 防止越权访问
 * 1️⃣ findByHost(User host) ——「这个人有哪些房源？」
     * -> 用在什么场景？
     * 场景 A：Host 后台管理页面 👉 只返回这个 host 自己的房源
 * 2️⃣ findByIdAndHost(Long id, User host) ——「这个房源是不是你的？」
     * -> 用在什么场景？
     * 场景：编辑 / 删除房源
 * 4️⃣ 从“系统设计角度”总结这两个方法
     * | 方法                | 解决的问题               |
     * | -----------------  | ---------------------- |
     * | `findByHost`       | **我是谁 → 我能看到什么** |
     * | `findByIdAndHost`  | **我能不能操作这个资源**  |
 * 5️⃣ 用一句“人话”帮你彻底记住
     * List 页面 → findByHost
     * Edit / Delete → findByIdAndHost
     * 不要相信前端传来的 id
     * 权限要在数据库查询层就卡死
 */

/**
 * 你顺便问的那句：“还能支持哪些搜索参数？”
 * 常见还能加的有（只列，不展开）：
     * priceBetween
     * bedNumber >=
     * bathroom >=
     * hasWifi
     * hasKitchen
     * propertyType
     * amenities IN (...)
 */