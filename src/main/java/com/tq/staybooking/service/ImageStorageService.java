package com.tq.staybooking.service;

/**
 * 1. Go to the com.tq.staybooking.service package and create a new service class called ImageStorageService.
 * 2. Add the bucket name as the private field and inject the value from the application.properties.
 * 3. Add the storage private field and implement the save() method to save the given image to GCS.
 * 4. Integrate ImageUploadService with StayService
 */

import com.google.cloud.storage.Acl;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.tq.staybooking.exception.GCSUploadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

@Service
public class ImageStorageService {
    // 只要是用 @Value 注入的字段，就 不需要、也不应该 放进 @Autowired 的 constructor 里。
    // 一、Spring 注入的三种“东西”，本质不同 -> Spring 注入的东西大致分 3 类：
    //| 注入的是什么                          | 常见方式                  | 要不要放 constructor |
    //| -------------------------------     | -------------------    | ----------------    |
    //| **Bean（对象）**                      | `@Autowired` / 构造函数 | ✅ 要                |
    //| **配置值（String / int / boolean）**   | `@Value`              | ❌ 不要              |
    //| **简单常量（写死的）**                  | `static final`         | ❌ 不需要            |
    @Value("${gcs.bucket}")
    private String bucketName;

    private Storage storage;

    @Autowired
    public ImageStorageService(Storage storage) {
        this.storage = storage;
    }
    // MultipartFile 是Spring 对 HTTP 上传文件的封装
    // 这里的返回值是个连接url
    // 前端：<input type="file" name="images" />; 后端就会收到：MultipartFile file
    // 它里面有：
        // 文件内容（InputStream）
        // 文件名
        // 文件大小
        // Content-Type
    public String save(MultipartFile file) throws GCSUploadException {
        /**
         * BlobId blobId = BlobId.of(bucketName, objectName);
         * BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
         * storage.create(blobInfo, File.readAllBytes(Paths.get(filePath)));
         * 改 storage.create(blobInfo, File.getInputStream());
         * return blobInfo.getMediaLink();
         */
        // 因为GCS bucket 里 文件名必须唯一（用户上传的文件名可能重名，中文，带空格）， 👉 UUID = 几乎不可能重复的名字
        String filename = UUID.randomUUID().toString();

        // Blob = GCS 里的“一个文件对象”
        // BlobInfo 就是：“我要创建的这个文件的说明书”
            // 里面描述：
            // 存在哪个 bucket
            // 文件名叫啥
            // Content-Type 是什么
            // 谁可以访问
        BlobInfo blobInfo = null;
        try {
            blobInfo = storage.createFrom( //核心代码
                    BlobInfo
                            .newBuilder(bucketName, filename) // 创建 BlobInfo
                            .setContentType("image/jpeg") // 设置文件类型
                            .setAcl(new ArrayList<>(Arrays.asList(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER)))) // 设置 ACL（访问权限）任何人（包括没登录的用户）都可以读取这个文件
                            .build(),
                    file.getInputStream()); // 传入文件内容: 图片的真实二进制数据
        } catch (IOException exception) {
            // try / catch 为啥只 catch IOException？
                // getInputStream() 可能失败
                // 网络上传可能失败
                // GCS SDK 会抛 IOException
            // 👉 你把 所有底层错误统一转成业务异常
            // 这一步非常重要：Controller / Service 不需要知道是“流坏了”还是“网络断了”，它只关心：上传失败
            throw new GCSUploadException("Failed to upload file to GCS");
        }

        return blobInfo.getMediaLink();
        // 为什么返回 blobInfo.getMediaLink()？
            // GCS 给你的 访问链接
            // 可以通过浏览器访问（前提是 ACL 是 public）
        // ⚠️ 但注意：在真实项目里，很多人会改成自己拼 URL（后面我可以讲）

    }
}
/**
 * 用一句“人话流程”总结整段代码
     * 给我一张图片
     * ↓
     * 生成一个不重复的名字
     * ↓
     * 告诉 GCS：我要在这个 bucket 创建一个 jpeg 文件
     * ↓
     * 设置成全世界可读
     * ↓
     * 把图片内容上传
     * ↓
     * 返回图片的访问地址
 */
/**
 * 一、Spring 注入的三种“东西”，本质不同
     * Spring 注入的东西大致分 3 类：
     * | 注入的是什么                          | 常见方式                | 要不要放 constructor |
     * | ------------------------------- | ------------------- | ---------------- |
     * | **Bean（对象）**                    | `@Autowired` / 构造函数 | ✅ 要              |
     * | **配置值（String / int / boolean）** | `@Value`            | ❌ 不要             |
     * | **简单常量（写死的）**                   | `static final`      | ❌ 不需要            |
 * 二、为什么 @Value 不放 constructor？（核心原因）
 * 1️⃣ 生命周期顺序不同（非常关键）
 * Spring 创建一个 Bean 的顺序是：
     * ① 调用构造函数（constructor）
     * ② 注入字段（@Autowired / @Value）
     * ③ 执行 @PostConstruct（如果有）
     * 🚨 构造函数执行的时候，@Value 还没生效,所以你根本没法在 constructor 里安全使用 @Value 的字段。
 * 2️⃣ @Value 本质是“配置注入”，不是依赖注入
     * @Value("${gcs.bucket}")
     * private String bucketName;
     * 这不是一个 Bean，而是：
     * 从配置文件里读一个值 → 塞进字段
     * Spring 不会、也不鼓励你写成：
     * public ImageStorageService(
     *     Storage storage,
     *     @Value("${gcs.bucket}") String bucketName   // ❌ 不推荐
     * )
 * 三、什么时候一定要放进 constructor？
 * ✅ 只要是 Bean 依赖，就应该进 constructor
 * 四、什么时候用 @Value 字段注入是“最佳实践”？
 * ✅ 配置项、环境变量、参数值
     * @Value("${gcs.bucket}")
     * private String bucketName;
     *
     * @Value("${jwt.secret}")
     * private String jwtSecret;
     *
     * @Value("${spring.datasource.url}")
     * private String datasourceUrl;
 * 五、一个你可以直接记住的“黄金规则”
 * 🧠 记忆版：
     * Bean → 构造函数注入
     * 配置值 → @Value 字段注入
     * 不要混着来
 */
/**
 * 下一步（强烈推荐） 还没讲解 先把课程走完吧
 * 我可以下一步帮你讲👇
 * 👉 为什么 getMediaLink() 有坑，以及更推荐的 URL 写法
 * 👉 MultipartFile 在 HTTP 里是怎么来的（form-data 结构）
 * 👉 如果上传多张图片，为什么用 parallel() 以及有什么坑
 */