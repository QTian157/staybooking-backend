package com.tq.staybooking.repository;

/**
 * 1. Under the same com.tq.staybooking.repository package, create a new interface called StayReservationDateRepository.
 */

import com.tq.staybooking.model.Location;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.GeoDistanceQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class CustomLocationRepositoryImpl implements CustomLocationRepository{
    private final String DEFAULT_DISTANCE= "50";
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    public CustomLocationRepositoryImpl(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @Override
    public List<Long> searchByDistance(double lat, double lon, String distance) {
        if (distance == null || distance.isEmpty()) {
            distance = DEFAULT_DISTANCE;
        }

        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withFilter(new GeoDistanceQueryBuilder("geoPoint").point(lat, lon).distance(distance, DistanceUnit.KILOMETERS));

        SearchHits<Location> searchResult = elasticsearchOperations.search(queryBuilder.build(), Location.class);

        List<Long> locationIDs = new ArrayList<>();
        for (SearchHit<Location> hit: searchResult.getSearchHits()){
            locationIDs.add(hit.getContent().getId());
        }

        return locationIDs;
    }
}

/**
 * Spring Data Elasticsearch = 用 Java 对象，去拼 ES Query DSL，再把 ES 返回的 JSON 包装成 Java 结果
 * 一、先给你一张“角色分工表”（先认人）
     * | 类                          | 它是干嘛的                             |
     * | -------------------------- | --------------------------------- |
     * | `NativeSearchQueryBuilder` | **用 Java 拼 Elasticsearch 查询 DSL** |
     * | `GeoDistanceQueryBuilder`  | **专门拼 geo_distance 条件**           |
     * | `ElasticsearchOperations`  | **真正把查询发给 ES 的“执行器”**             |
     * | `SearchHits<T>`            | **一次搜索的“完整返回结果”**                 |
     * | `SearchHit<T>`             | **单条命中的结果（一条文档）**                 |
 * 二、为什么要用 NativeSearchQueryBuilder？
     * 1️⃣ Spring Data ES 有两种查询方式
         * ❌ 派生查询（你这个用不了
         * findByXxx()
         * 只能用于简单字段查询，不支持 geo_distanc
     * ✅ Native 查询（你现在用的）
         * NativeSearchQueryBuilder
         * 它的真实含义是：“我要直接用 Elasticsearch 原生 DSL，只是用 Java 写”
         * 也就是这个 JSON 的 Java 版本 👇
             * {
             *   "query": {
             *     "bool": {
             *       "filter": {
             *         "geo_distance": {
             *           "distance": "50km",
             *           "geoPoint": {
             *             "lat": 38.6,
             *             "lon": -90.2
             *           }
             *         }
             *       }
             *     }
             *   }
             * }
         * 👉 只要你涉及 geo / 聚合 / bool / filter / must / should
         * 👉 99% 都要用 NativeSearchQueryBuilder
     * 2️⃣ 那 GeoDistanceQueryBuilder 又是啥？-> GeoDistanceQueryBuilder = geo_distance DSL 的 Java 版
         * new GeoDistanceQueryBuilder("geoPoint")
         *     .point(lat, lon)
         *     .distance(distance, DistanceUnit.KILOMETERS)
         * 它就是在帮你拼这一段 👇
             * "geo_distance": {
             *   "distance": "50km",
             *   "geoPoint": {
             *     "lat": ...,
             *     "lon": ...
             *   }
             * }
 * 三、elasticsearchOperations 是干嘛的？
     * SearchHits<Location> searchResult =
     *     elasticsearchOperations.search(query, Location.class);
     * 它的角色是：Spring Data ES 的“底层执行引擎”
 * 你可以把它理解成：
     * JDBC Template（对数据库）
     * RestTemplate（对 HTTP）
     * 👉 它负责：
         * 把 Query 转成 ES 请求
         * 发给 ES
         * 把 ES 返回的 JSON 反序列化成 Java 对象
 * 四、为什么返回的是 SearchHits<Location>，而不是 List<Location>？
 * 1️⃣ ES 返回的远不只是“数据”
     * ES 一次搜索返回的是👇
     * 命中总数（total hits）
     * 每条文档的 _score
     * _id
     * _source
     * 高亮
     * 排序值
     * shard 信息
     * 👉 所以 Spring Data ES 用一个**“总包装”**来装：
     * SearchHits<Location>
 * 2️⃣ SearchHits<T> 是什么？
     * 它代表：“一次搜索的完整结果集”
     * 里面包括：
         * getTotalHits()
         * getSearchHits()（真正的每条命中）
 * 3️⃣ 那 SearchHit<Location> 又是啥？
     * for (SearchHit<Location> hit : searchResult.getSearchHits()) {
     *     Location location = hit.getContent();
     * }
     * SearchHit<T> = 一条 ES 文档的完整信息
     * 它包含：
         * _id
         * _score
         * _source → Location
     * 你现在只用到了：
         * hit.getContent()
         * 也就是 _source 反序列化后的 Location
 * 五、把整条调用链“翻译成人话”
     * 1️⃣ 用 NativeSearchQueryBuilder 拼一个 geo_distance 查询
     * 2️⃣ 用 ElasticsearchOperations 把查询发给 ES
     * 3️⃣ ES 返回很多文档
     * 4️⃣ Spring Data 把每条文档包成 SearchHit<Location>
     * 5️⃣ 我只拿 _source.id，组成一个 id 列表
 */