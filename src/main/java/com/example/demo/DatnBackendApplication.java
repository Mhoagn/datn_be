package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableAsync
@EnableScheduling
public class DatnBackendApplication {

	public static void main(String[] args) {
		// Railway mặc định UTC — set sớm trước Spring để LocalDateTime.now() đúng giờ VN
		java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
		SpringApplication.run(DatnBackendApplication.class, args);
	}


}
