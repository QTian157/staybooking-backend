package com.tq.staybooking.repository;

import com.tq.staybooking.model.StayReservedDate;
import com.tq.staybooking.model.StayReservedDateKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * 1. Under the same com.tq.staybooking.repository package, create a new interface called StayReservationDateRepository.
 * 2. Add a method named findByIdInAndDateBetween()
     * so that we can search results only contain stays that are reserved between check-in date and checkout date.
 * Obviously, the JpaRepository cannot support the custom findByIdInAndDateBetween() method,
 * so we need to provide the implementation by ourselves.
     * We can use the same solution as LocationRepository to create an implementation class,
     * or in this case, just write the SQL query on top of the method.
 * 3. Go to the StayRepository interface and add a new method called findByIdInAndGuestNumberGreaterThanEqual().
     * So besides location, the guest number is another parameter for search.
     * Can you think of some other search parameters we can support?
 */

@Repository
public interface StayReservationDateRepository extends JpaRepository<StayReservedDate, StayReservedDateKey> {

    // 那什么时候才“应该”用 Set<Long>？
    // 当返回值只是“标识符集合 / 中间过滤结果”时，用 Set<Long>
    @Query(value = "SELECT srd.id.stayId FROM StayReservedDate srd WHERE srd.id.stayId IN ?1 AND srd.id.date BETWEEN ?2 AND ?3 GROUP BY srd.id.stayId")
    Set<Long> findByIdInAndDateBetween(List<Long> stayIds, LocalDate startDate, LocalDate endDate);
}
/**
 * 一、Spring Data Repository 一共有「三种」查询来源
     * ✅ 第一种：方法名派生（最简单）:
         * findByUserIdAndStatus(...)
     * ✅ 第二种：
         * @Query: @Query("SELECT ...")
         * Set<Long> findByIdAndDateBetween(...)
     * ✅ 第三种：CustomRepository + Impl（最自由）
         * CustomLocationRepositoryImpl
 * 二、你这个例子为什么 非常适合 @Query？
     * SELECT srd.id.stay_id
     * FROM StayReservedDate srd
     * WHERE srd.id.stay_id IN ?1
     *   AND srd.id.date BETWEEN ?2 AND ?3
     * GROUP BY srd.id.stay_id
     * 特点非常明显：
         * ✅ 单表（StayReservedDate）
         * ✅ 条件清晰（IN + BETWEEN）
         * ✅ 不涉及复杂业务逻辑
         * ✅ 返回的是标量值（Long）
         * 👉 这是 JPA 的“舒适区”
 * 三、为什么这里不用 CustomRepository + Impl？
     * 你可以对比一下 Location 的 geo 查询 👇
         * | 场景           | 能不能用 @Query |
         * | ------------ | ----------- |
         * | MySQL 日期区间   | ✅           |
         * | IN + BETWEEN | ✅           |
         * | group by     | ✅           |
         * | geo_distance | ❌（JPA 不支持）  |
         * | ES DSL       | ❌           |
     * 👉 Location 那个必须走 CustomRepository + Impl
     * 👉 StayReservedDate 这个完全没必要
 * 四、为什么不用方法名派生？比如这样？
     * findByIdStayIdInAndIdDateBetween(...)
     * 理论上可以，但：
         * 方法名会非常长
         * 可读性差
         * 容易写错（嵌套 id）
         * group by 很难优雅表达
     * 👉 这种情况，@Query 是最优解
 * 五、你现在这段代码在“架构上”的位置（非常重要）
     * 你现在的系统已经自然形成了一个非常标准的分层：
         * | 存储                | 查询方式                    |
         * | ----------------- | ----------------------- |
         * | MySQL（结构化）        | JpaRepository + @Query  |
         * | Elasticsearch（搜索） | CustomRepository + Impl |
         * | 简单 CRUD            | 方法名派生                   |
 * 六、一句话帮你彻底记住：
     * @Query =“我自己写 SQL / JPQL，但让 Spring Data 帮我执行”
     * CustomRepository =“这已经超出 Repository 的能力边界了，我自己全权处理”
 */

/**
 * 一、srd 是啥？-> srd 是一个“临时别名”，代表 StayReservedDate 这张表 / 这个实体
 * 你原来的 JPQL:
     * SELECT srd.id.stay_id
     * FROM StayReservedDate srd
     * 可以拆成两部分看：
         * 1️⃣ StayReservedDate 是 JPA 实体类名
             * 对应数据库里的表（比如 stay_reserved_date）
         * 2️⃣ srd是你自己起的缩写名
             * 作用：后面引用字段时少打字
 * 二、srd.id.stay_id 是啥结构？
     * -> 你这个实体是 复合主键，类似这样：
     * @Entity
     * public class StayReservedDate {
     *
     *     @EmbeddedId
     *     private StayReservedDateKey id;
     * }
     * -> srd.id.stayId为什么不是srd.id.stay_Id: 参数需要时java字段名 不是数据库列名
         * srd.id.stayId ✅（Java 字段名）
         * 不是 srd.id.stay_id ❌（数据库列名）
 * 三、WHERE srd.id.stay_id IN ?1 是啥意思？
     * -> 只要 stay_id 在我给你的那一堆 id 里面
     * -> 那 ?1 是啥？
         * ?1 是“第一个方法参数”
         * Set<Long> findByIdAndDateBetween(
         *     List<Long> stayIds,      // ← 第 1 个参数 → ?1
         *     LocalDate startDate,     // ← 第 2 个参数 → ?2
         *     LocalDate endDate        // ← 第 3 个参数 → ?3
         * );
     * -> 所以：
         * | JPQL | 方法参数        |
         * | ---- | ----------- |
         * | `?1` | `stayIds`   |
         * | `?2` | `startDate` |
         * | `?3` | `endDate`   |
     * -> IN ?1 再翻译一遍
         * IN ?1 再翻译一遍
     * 等价于：
         * srd.id.stay_id IN (1, 5, 9, 20) -> （假设 stayIds = [1,5,9,20]）
 * 五、GROUP BY srd.id.stay_id 是干嘛的？
     * -> 结论先给你: 去重用的
     * -> 为啥要 group？
         * 同一个 stay：
             * 可能被订了好几天
             * 表里会有多条记录
         * GROUP BY srd.id.stay_id = “每个 stay 只返回一次”
 *
 */