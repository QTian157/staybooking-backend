package com.tq.staybooking.config;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;

/**
 * 1. Go to the com.tq.staybooking.config package and create a new class named ElasticsearchConfig.
 * 2. Add the elasticsearchClient() method to create a Elasticsearch client bean.
 * 3. Go to com.tq.staybooking.exception package and create a new exception InvalidSearchDateException.
 */

@Configuration
public class ElasticsearchConfig extends AbstractElasticsearchConfiguration {

    @Value("${elasticsearch.address}")
    private String elasticsearchAddress;

    @Value("${elasticsearch.username}")
    private String elasticsearchUsername;

    @Value("${elasticsearch.password}")
    private String elasticsearchPassword;

    // 这里不能写成restHighLevelClient()
    // 要写成elasticsearchClient() 因为你继承的是 AbstractElasticsearchConfiguration，
        // 但你 override 的方法名写成了 restHighLevelClient()
        // 父类要求你实现的是 elasticsearchClient()
        // 所以它说：你没有实现抽象方法，同时 @Override 也找不到对应父类方法。
    // 这里要写成@Bean而不是@Override 因为父类没有@Bean，而且这是自己定义的bean

    @Override
    @Bean
    public RestHighLevelClient elasticsearchClient(){
        ClientConfiguration clientConfiguration
                = ClientConfiguration.builder()
                .connectedTo(elasticsearchAddress)
                .withBasicAuth(elasticsearchUsername, elasticsearchPassword)
                .build();
        return RestClients.create(clientConfiguration).rest(); // 用配置生成真正 client
    }
}
/**
 * In contrast, we didn’t create MySQL client bean manually in our code.
 * Because Spring Boot can help create one based on the information we put in the application.properties file.
 */