package com.tq.staybooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication

@EntityScan("com.tq.staybooking") // 改成你项目的根包
public class StaybookingApplication {

	public static void main(String[] args) {
		SpringApplication.run(StaybookingApplication.class, args);
	}

}
