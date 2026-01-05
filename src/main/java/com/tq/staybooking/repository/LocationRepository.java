package com.tq.staybooking.repository;

import com.tq.staybooking.model.Location;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * 1. Create an interface named LocationRepository under the com..staybooking.repository package.
     * As you can see, the LocationRepository extends ElasticsearchRepository instead of JpaRepository
     * since Elastcisearch has a different query implementation than MySQL.
     * But similar to JpaRepository,
     * LocationRepository also provides some basic query functions like find(), save() and delete().
     * But since our service needs to support search based on Geolocation,
     * we need to implement the search function ourselves.
 * 2. Create another CustomLocationRepository interface next to the LocationRepository interface,
     * and add a method called CustomLocationRepository().
 * 3. Make the LocationRepository interface extend the CustomLocationRepository interface.
 */

@Repository
public interface LocationRepository extends ElasticsearchRepository<Location, Long>, CustomLocationRepository{

}

/**
 * 为什么不能“直接继承就有 searchByDistance
 * 1️⃣ ElasticsearchRepository 能自动实现的，只有两类方法
     * ✅ 第一类：CRUD
         * save()
         * findById()
         * deleteById()
     * ✅ 第二类：简单方法名派生查询
         * findByUserId(Long userId)
         * findByPriceBetween(...)
         * 这些必须满足：
         * 基于字段
         * 等值 / range
         * 不涉及复杂 ES DSL
 * 2️⃣ searchByDistance 属于哪一类？
     * searchByDistance(double lat, double lon, String distance)
     * 它本质是：
         * geo_distance 查询
         * 需要构造 ES Query DSL
         * 需要 ElasticsearchOperations / NativeQuery
     * 👉 这已经超出了 Spring Data 的“方法名自动生成能力”
         * Spring Data 不可能从方法名里推导出：
         * {
         *   "geo_distance": {
         *     "distance": "10km",
         *     "geoPoint": { "lat": ..., "lon": ... }
         *   }
         * }
     * 所以结论是：❌ 不是“继承少了”，而是“框架压根不支持”
 * 那为什么要有 CustomLocationRepository 这个接口？
     * public interface CustomLocationRepository {
     *     List<Long> searchByDistance(double lat, double long, String distance);
     * }
     * 👉 声明：我需要一个“自定义查询能力”，Spring Data 自动做不了
     * 注意几个点：
         * 这是 接口
         * 没有 extends
         * 没有 @Repository（先别急）
     * 它的作用是：
         * 👉 定义“你想要什么能力”
 * 那真正的“底层实现”在哪里？关键在这条 Spring Data 约定（非常重要）👇
     * 规则（死记住）
         * 如果你有：
             * interface LocationRepository
             *     extends ElasticsearchRepository<Location, Long>,
             *             CustomLocationRepository
     * 那 Spring 会去找：
         * class CustomLocationRepositoryImpl
     * 并把它 拼装进 LocationRepository
     * 也就是说：
         * ElasticsearchRepository → Spring 自动实现
         * CustomLocationRepository → 你自己实现
         * Spring 在运行期把两者 合并成一个 Bean
     * 这叫：
         * 👉 Repository Fragment（仓库碎片拼接）
 * 那为什么实现类要加 @Repository？
     * 1️⃣ 接口上加不加 @Repository 没意义: 接口只是声明，不是 Bean。
         * public interface CustomLocationRepository { ... }
     * 2️⃣ 真正要被 Spring 管理的是：实现类
         * @Repository
         * public class CustomLocationRepositoryImpl
         *         implements CustomLocationRepository {
         *     ...
         * }
         * @Repository 的作用是：
             * 把这个类注册为 Spring Bean
             * 让它参与异常转换（PersistenceExceptionTranslation）
         * 👉 不是为了“继承方法”
         * 👉 而是为了 能被 Spring 找到并注入
 * 为什么LocationRepository里面不放searchByDistance 但是CustomLocationRepository放？
     * 不是“接口里不能有方法”，而是：Spring Data 会“如何解读这个接口里的方法”不一样
         * -> LocationRepository 里的方法 → Spring Data 会“自动解析”
         * -> CustomLocationRepository 里的方法 → Spring Data“完全不解析”
 * 1️⃣ LocationRepository 是 Spring Data 主仓库接口
     * 只要一个接口：
         * extends ElasticsearchRepository
         * 被 Spring 扫描为 Repository
         * 👉 Spring Data 就会“接管”它里面的每一个方法
     * 然后按规则尝试：
         * 派生查询（方法名解析）
         * @Query
         * 内置 CRUD
 */