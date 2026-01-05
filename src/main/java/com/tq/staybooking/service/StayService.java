package com.tq.staybooking.service;

import com.tq.staybooking.exception.StayDeleteException;
import com.tq.staybooking.exception.StayNotExistException;
import com.tq.staybooking.model.*;
import com.tq.staybooking.repository.LocationRepository;
import com.tq.staybooking.repository.ReservationRepository;
import com.tq.staybooking.repository.StayRepository;
import com.tq.staybooking.repository.StayReservationDateRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 1. Go to the com.tq.staybooking.service package and create a new class StayService.
 * 2. Add the StayRepository as a private field and create a constructor for initialization.
 * 3. Implement the methods for stay save, delete by id, list by the user and get by id.
 * 4. Integrate ImageUploadService with StayService
     * -> Open StayService class and add ImageStorageService as a private field.
     * -> Update the add() method to support image saving.
     * Upload stay upload API in StayController to read images from requests and pass them to StayService.
 * 5. Update the StayService to save location information to Elasticsearch.
 * 6. Go to CustomExceptionHandler class to include GeoCodingException and InvalidStayAddressException.
 * 7. Go to the StayService class and update the delete() method with ReservationRepository/ StayReservationDateRepository.
 * 8. Under com.tq.staybooking.exception package, create InvalidReservationDateException class.
 */
@Service
public class StayService {
    private StayRepository stayRepository;

    private ImageStorageService imageStorageService;

    private LocationRepository locationRepository;
    private GeoCodingService geoCodingService;

    private ReservationRepository reserveRepository;
    private StayReservationDateRepository stayReservationDateRepository;


    @Autowired
    public StayService(StayRepository stayRepository, ImageStorageService imageStorageService, LocationRepository locationRepository, GeoCodingService geoCodingService, ReservationRepository reserveRepository,StayReservationDateRepository stayReservationDateRepository ) {
        this.stayRepository = stayRepository;
        this.imageStorageService = imageStorageService;
        this.locationRepository = locationRepository;
        this.geoCodingService = geoCodingService;
        this.reserveRepository= reserveRepository;
        this.stayReservationDateRepository = stayReservationDateRepository;
    }

    public List<Stay> listByUser(String username){
        return stayRepository.findByHost(new User.Builder().setUsername(username).build());
    }

    public Stay findByIdAndHost(Long stayId, String username) throws StayNotExistException {
        Stay stay = stayRepository.findByIdAndHost(stayId, new User.Builder().setUsername(username).build());
        if (stay == null){
            throw new StayNotExistException("Stay doesn't exist.");
        }
        return stay;
    }

    public void add(Stay stay) {
        stayRepository.save(stay);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void delete(Long stayId, String username) throws StayNotExistException, StayDeleteException {
        Stay stay = stayRepository.findByIdAndHost(stayId, new User.Builder().setUsername(username).build());
        if (stay == null) {
            throw new StayNotExistException("Stay doesn't exist.");
        }
        List<Reservation> reservations = reserveRepository.findByStayAndCheckoutDateAfter(stay, LocalDate.now());
        if (reservations != null && !reservations.isEmpty()) {
            throw new StayDeleteException("Cannot delete stay with active reservation");
        }
        stayRepository.delete(stay);
    }

    /**
     * 创建一个 Stay，同时把用户上传的多张图片：
     * 先存到 GCS → 拿到每张图片的 URL → 再把这些 URL 和 Stay 的关系一起存进数据库
     */

    @Transactional(isolation = Isolation.SERIALIZABLE)
    // 存储stay和图片url必须同时成功 要不然就回滚
    // Isolation.SERIALIZABLE: 数据库事务的“最严格等级”。

    public void add(Stay stay, MultipartFile[] images) {
        List<String> mediaLinks = Arrays.stream(images).parallel().map(image -> imageStorageService.save(image)).collect(Collectors.toList());
        List<StayImage> stayImages = new ArrayList<>();
        for (String mediaLink : mediaLinks) {
            stayImages.add(new StayImage(mediaLink, stay));
        }
        stay.setImages(stayImages);

        stayRepository.save(stay);

        // add location
        Location location = geoCodingService.getLatLng(stay.getId(), stay.getAddress());
        locationRepository.save(location);
    }
}
/**
 * 1️⃣ 为什么一定是 stayRepository.save(stay) 之后？ 👉 让数据库生成 stay.id
     * Location 要用 stay.id 作为 ES 文档 id
     * 如果还没 save stay：
         * stay.getId() 是 null
         * ES 根本不知道和哪个 stay 关联
     * 所以顺序是必然的：先 MySQL → 再 ES
 * 2️⃣ geoCodingService.getLatLng(...) 真正在做什么？
     * 输入是什么？
         * stay.getId()
         * 👉 数据库里已经存在的主键
         * stay.getAddress()
         * 👉 前端传来的「人类地址」
     * GeoCodingService 内部做了什么？
         * address → Google → lat/lng → GeoPoint → Location
         * 它不是存数据，它只是“算数据”。
 * 3️⃣ 那 Location 到底是什么？ Location 是：Elasticsearch 专用的“搜索索引对象”
     * 它不是：
         * JPA Entity（不进 MySQL）
         * 前端 DTO
     * 它只服务一件事：
         * 👉 geo-based search
 * 4️⃣ locationRepository.save(location) 在干嘛？
     * 这一步非常关键：
         * 把 { id, geo_point } 存进 Elasticsearch
         * 不是 MySQL
         * 用的是 ElasticsearchRepository
     * 等价于 ES 层的：
         * PUT /location/_doc/123
         * {
         *   "id": 123,
         *   "location": {
         *     "lat": 37.42,
         *     "lon": -122.08
         *   }
         * }
 * 不是直接存地址，而是：
     * 👉 把前端传来的地址
     * 👉 在后端立即转换成经纬度
     * 👉 用 stayId 作为关联 key
     * 👉 存进 Elasticsearch，供以后做距离搜索
 * 再讲一个你现在“可能会误解的点”（非常重要）
     * ❗ 这个 @Transactional 并不能真正回滚 Elasticsearch
     * ✔️ MySQL：能回滚
     * ❌ Elasticsearch：不能回滚: ES 不在同一个事务管理器里 -> Spring 的 @Transactional 管不到 ES 教学用这么写
 * 再给你一个“脑内流程图”（帮助你记住）
     * 前端提交 stay
     *     ↓
     * Controller
     *     ↓
     * StayService.add()
     *     ↓
     * MySQL: stayRepository.save()
     *     ↓ (得到 stayId)
     * GeoCodingService
     *     ↓
     * Google Geocoding API
     *     ↓
     * lat / lng
     *     ↓
     * Elasticsearch: locationRepository.save()
 *
 */