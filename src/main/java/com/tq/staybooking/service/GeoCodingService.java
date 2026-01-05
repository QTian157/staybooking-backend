package com.tq.staybooking.service;


import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import com.tq.staybooking.exception.GeoCodingException;
import com.tq.staybooking.exception.InvalidStayAddressException;
import com.tq.staybooking.model.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.stereotype.Service;


import java.io.IOException;

/**
 * 1. Go to the com.tq.staybooking.service package, create a new class called GeoCodingService.
 * 2. Implement the getLatLong method based on the GeoCoding API.
 * 3. Update the StayService to save location information to Elasticsearch.
 */

// 告诉 Spring：👉 这是一个“业务服务类”
@Service
public class GeoCodingService {
    // GeoApiContext 是什么？ -> Google Maps Java SDK 的“客户端配置对象”
    // 里面已经装好了:
        // API Key
        // 网络配置
    // 所以这里相当于在说：“Spring，把配置好的 Google 客户端给我”
    private GeoApiContext context;

    @Autowired
    public GeoCodingService(GeoApiContext context){
        this.context = context;
    }

    // 这个方法“对外承诺”什么？
        //-> 输入：
            //id：stay 的 id（数据库生成的）
            //address：人类地址字符串
        //-> 输出：
            //Location：
            //id = stayId
        //-> location = GeoPoint(lat, lng)
    // 4️⃣ 调 Google 把地址变成经纬度:
        //    GeocodingResult[] results =
        //            GeocodingApi.geocode(context, address).await();

    public Location getLatLng(Long id, String address) throws GeoCodingException {
        try {
            GeocodingResult result = GeocodingApi.geocode(context, address).await()[0];
            if (result.partialMatch) {
                throw new InvalidStayAddressException("Failed to find stay address");
            }
            return new Location(
                    id,
                    new GeoPoint(
                            result.geometry.location.lat,
                            result.geometry.location.lng
                    )
            );
        } catch (IOException | ApiException | InterruptedException e) { // Google API 可能失败的原因：网络/ Key 配置/ Google 服务异常/ 线程被打断
            e.printStackTrace();
            throw new GeoCodingException("Failed to encode stay address");
        }
    }
}

/**
 * 一。 GeoCodingService 的作用只有一件事：
     * 👉 把「人能读的地址字符串」
     * 👉 交给 Google
     * 👉 换成「机器能算距离的经纬度」
     * 👉 封装成 Location，交给 Elasticsearch
 * 它不做搜索、不存数据库、不管权限。
 * 它只是一个“地址 → 经纬度翻译器”。
 * 二。 放在整个 StayBooking 系统里的位置
 * 想象新增一个 stay 的流程：
     * Controller
     *   ↓
     * StayService.add()
     *   ↓
     * GeoCodingService.getLatLng()
     *   ↓
     * Google Geocoding API
     *   ↓
     * (lat, lng)
     *   ↓
     * Location(id, geo_point)
     *   ↓
     * Elasticsearch
 */