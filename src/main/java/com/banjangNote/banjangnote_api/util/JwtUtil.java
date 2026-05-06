package com.banjangNote.banjangnote_api.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    // 🚨 실무에서는 application.yml에 길고 복잡한 비밀키를 숨겨두어야 합니다!
    // 지금은 테스트용으로 메모리에 생성하겠습니다.
    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // 토큰 만료 시간 (예: 24시간)
    private final long EXPIRATION_TIME = 1000 * 60 * 60 * 24;

    // 🔥 토큰 생성 메서드
    public String generateToken(String email, String role) {
        return Jwts.builder()
                   .setSubject(email)
                   .claim("role", role)
                   .setIssuedAt(new Date())
                   .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                   .signWith(key)
                   .compact();
    }

    // 🔥 토큰을 해독해서 이메일(Subject)을 꺼내는 메서드 추가
    public String getEmailFromToken(String token) {
        return Jwts.parserBuilder()
                   .setSigningKey(key) // 우리가 만든 비밀키로 자물쇠를 엽니다.
                   .build()
                   .parseClaimsJws(token)
                   .getBody()
                   .getSubject(); // 발급할 때 setSubject(email)로 넣었던 그 이메일!
    }
}
