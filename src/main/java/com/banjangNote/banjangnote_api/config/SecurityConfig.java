package com.banjangNote.banjangnote_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // 비밀번호 암호화 객체 생성!
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configure(http)) // 우리가 만든 WebConfig의 CORS 설정을 그대로 씁니다.
                .csrf(csrf -> csrf.disable()) // REST API이므로 CSRF 방어는 끕니다.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // JWT를 쓸 거라 세션은 안 씁니다.
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // 🚨 일단 테스트를 위해 모든 API 접근을 허용해 둡니다! (나중에 잠글 예정)
                );
        return http.build();
    }
}
