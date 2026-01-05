package com.tq.staybooking.config;


import com.google.maps.GeoApiContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 1. Go to the con.tq.staybooking.config package, create GoogleGeoCodingConfig class to provide GeoApiContext.
 * 2. Go to the com.tq.staybooking.exception package,
     * create GeoCodingException which will be thrown if there’s any exception when we connect to Geolocation API.
 */

@Configuration
public class GoogleGeoCodingConfig {

    @Value("${geocoding.apikey}")
    private String apiKey;

    @Bean
    public GeoApiContext geoApiContext(){
        return new GeoApiContext.Builder().apiKey(apiKey).build();
    }
}
