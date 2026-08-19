package com.banjangNote.banjangnote_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
@SpringBootApplication
public class BanjangnoteApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(BanjangnoteApiApplication.class, args);
	}

}
