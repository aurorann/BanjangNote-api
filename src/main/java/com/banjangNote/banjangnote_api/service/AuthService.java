package com.banjangNote.banjangnote_api.service;

import com.banjangNote.banjangnote_api.entity.Member;
import com.banjangNote.banjangnote_api.repository.MemberRepository;
import com.banjangNote.banjangnote_api.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service // 🔥 스프링에게 "이거 공통 서비스야!" 라고 알려줍니다.
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;

    public Member getMemberFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("로그인이 필요하거나 토큰이 없습니다.");
        }
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.getEmailFromToken(token);

        return memberRepository.findByEmail(email)
                               .orElseThrow(() -> new RuntimeException("해당 회원을 찾을 수 없습니다."));
    }
}
