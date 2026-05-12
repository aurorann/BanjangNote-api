package com.banjangNote.banjangnote_api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "https://banjangnote.vercel.app/",  // 실제 배포된 Vercel 주소 (필수)
                        "http://localhost:3000",            // 로컬 개발 포트
                        "http://127.0.0.1:3000"             // 루프백 주소 추가(예비)
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600); // 1시간 동안 OPTIONS 요청 결과 기억
    }
}
