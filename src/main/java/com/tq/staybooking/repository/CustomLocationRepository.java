package com.tq.staybooking.repository;

import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 1. Create another CustomLocationRepository interface next to the LocationRepository interface,
     * and add a method called CustomLocationRepository().
 * 2. Make the LocationRepository interface extend the CustomLocationRepository interface.
     * CustomRepository 接口 = 能力清单
     * Impl 类 = 真正干活的人
     * Spring Data = 中介，负责把人和清单拼在一起
 * 3. Create a CustomLocationRepositoryImpl class
 *
 */

public interface CustomLocationRepository{
    List<Long> searchByDistance(double lat, double lon, String distance);
}
