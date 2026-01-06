package com.tq.staybooking.config;

/**
 * Go to the com.tq.staybooking.config package and create a class to provide GCS connections.
 */

import com.google.auth.Credentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

// 建立一个GCS的连接
//@Configuration
//public class GoogleCloudStorageConfig {
//
//    @Bean //spring 管理
//    public Storage storage() throws IOException {
//        // 根据当前class条件下找到文件夹里的文件
//        // getClass().getClassLoader().getResourceAsStream("credentials.json")
//        Credentials credentials = ServiceAccountCredentials.fromStream(getClass().getClassLoader().getResourceAsStream("credentials.json"));
//        Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
////        return StorageOptions.newBuilder().setCredentials(credentials).build().getService();
//        return storage;
//    }
//}

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoogleCloudStorageConfig {

    @Bean
    public Storage storage() {
        // auto read environment variables GOOGLE_APPLICATION_CREDENTIALS to json
        return StorageOptions.getDefaultInstance().getService();
    }
}

