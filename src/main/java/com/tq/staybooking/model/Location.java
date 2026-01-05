package com.tq.staybooking.model;



import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;


//import javax.persistence.Id; 有@Entity的时候用

/**
 * 1. Go to the com.tq.staybooking.model package, create a new class called Location.
 * 2. Add the id and geopoint as the private field and the corresponding getters/setters to the Location class.
 * 3. Add the Elasticsearch related annotations so that we can create the mapping between the Location class and an Elasticsearch document.
 * 4. Create an interface named LocationRepository under the com..staybooking.repository package.
 */

@Document(indexName = "loc")
// indexName相当于MySQL的table
public class Location {

    // @Id:这是 Spring Data 通用注解。不是 MySQL / JPA 专属
    // @Id 负责告诉 ES：谁是 _id
    // @Field 负责告诉 ES：这个字段在 _source 里怎么建 mapping
    @Id
    @Field(type = FieldType.Long)
    private Long id;

    @GeoPointField
    // 必须加GeoPoint要不然 Elasearch不知道是计算距离
    private GeoPoint geoPoint;

    // 为什么 不需要无参构造函数？
    public Location(Long id, GeoPoint geoPoint) {
        this.id = id;
        this.geoPoint = geoPoint;
    }

    // 为什么getter不传参数： getter 的职责只有一个：把对象当前的状态“读出来”
    public Long getId(){
        return id;
    }
    // 为什么 不需要 setter？



}

/**
 * 为什么 不需要无参构造函数？
 * 1️⃣ 无参构造函数是给“框架反射用的”
 *     // | 场景           | 是否需要无参构造 |
 *     // | ------------ | -------- |
 *     // | JPA Entity   | ✅ 必须     |
 *     // | Jackson 反序列化 | ✅ 通常需要   |
 *     // | Spring 容器    | ✅        |
 *     // | 普通 Java 业务对象 | ❌ 不一定    |
 *
 * 2️⃣ 这里的 Location 是什么角色？ 它不是 Entity，也不是 Controller DTO,
 *     // 而是一个ES 查询 / 结果封装用的 Value Object
 *         // 生命周期短
 *         // 明确构造方式
 *         // 不需要被“反射生成”
 *         // 👉 所以你手动 new 的 new Location(id, geoPoint) 既然你 明确知道创建时必须有 id 和 geoPoint, 那 无参构造反而是有害的
 * 为什么getter不传参数： getter 的职责只有一个：把对象当前的状态“读出来”
 * 为什么 不需要 setter？
 *     // 1️⃣ setter 的本质：允许对象被“随意改”
 *     // 2️⃣ 但这个 Location 需要“可变”吗？你并不希望：查询过程中坐标被改, id 被替换 👉 所以它是一个不可变对象（effectively immutable）
 *     // 3️⃣ 不加 setter = 明确设计信号 ⚠️ 这个对象：创建完就固定, 不允许中途改, 只是一个“数据载体”
 * @Id： 这是 Spring Data 通用注解。
     *  -> Spring Data 的设计是这样的：
         *  | 模块                        | 底层存储               |
         * | ------------------------- | ------------------ |
         * | Spring Data JPA           | MySQL / PostgreSQL |
         * | Spring Data MongoDB       | MongoDB            |
         * | Spring Data Elasticsearch | Elasticsearch      |
         *
     * -> 👉 @Id 是一个“统一接口层”的概念: “这是这个文档 / 实体 / 记录的唯一标识”
         * 至于底层是：
         * MySQL 的 id 列
         * MongoDB 的 _id
         * ES 的 _id
         * 由具体模块去映射
 * 2️⃣ 在 ES 里，@Id 映射到什么？
     *  Elasticsearch 的 _id
         *  @Id
         * private Long id;
     * 等价于：
         * {
         *   "_id": "123"
         * }
     * ⚠️ 注意：_id 不是 mapping 里的 field, 它是 ES 文档的元数据
 * 为什么会有@Field
     * @Id 管的是「文档身份」： 👉 映射到 ES 的 _id
     * @Field 管的是「文档内容结构」： 👉 告诉 ES：如果这个字段存在于 _source，它的类型是什么
     * 也就是说，它只影响：
         * "_source": {
         *   "id": 123
         * }
     * 对应的 mapping：
         * "id": {
         *   "type": "long"
         * }
     * 同时加呈现的maping是啥@Id
         * @Field(type = FieldType.Long)
         * private Long id;
         * @GeoPointField
         * private GeoPoint geoPoint;
     * 一、结论先给你（最重要）
         * ES 里会同时存在两层东西：
             * 文档元数据 _id
             * mapping + _source 里的字段
             * 👉 mapping（简化版）长这样：
             * {
             *   "mappings": {
             *     "properties": {
             *       "id": {
             *         "type": "long"
             *       },
             *       "geoPoint": {
             *         "type": "geo_point"
             *       }
             *     }
             *   }
             * }
     * 假设你存了一条：new Location(123L, new GeoPoint(38.63, -90.20))
         * ES 实际存的是👇
             * {
             *   "_id": "123",
             *   "_source": {
             *     "id": 123,
             *     "geoPoint": {
             *       "lat": 38.63,
             *       "lon": -90.20
             *     }
             *   }
             * }
 */
