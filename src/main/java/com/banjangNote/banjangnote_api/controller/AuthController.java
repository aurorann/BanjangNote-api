package com.banjangNote.banjangnote_api.controller;

import com.banjangNote.banjangnote_api.entity.Member;
import com.banjangNote.banjangnote_api.repository.MemberRepository;
import com.banjangNote.banjangnote_api.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder; // 암호화 도구
    private final JwtUtil jwtUtil; // 토큰 발급기

    // 📝 1. 회원가입 API
    @PostMapping("/signup")
    public String signup(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String rawPassword = request.get("password");
        String name = request.get("name");

        if (memberRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }

        Member newMember = new Member();
        newMember.setEmail(email);
        newMember.setName(name);
        // 🔥 입력받은 비밀번호를 해시(암호화)해서 저장합니다!
        newMember.setPassword(passwordEncoder.encode(rawPassword));

        memberRepository.save(newMember);
        return "회원가입 완료!";
    }

    // 🔓 2. 로그인 API
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String rawPassword = request.get("password");

        // 1. 유저 찾기
        Member member = memberRepository.findByEmail(email)
                                        .orElseThrow(() -> new RuntimeException("가입되지 않은 이메일입니다."));

        // 2. 비밀번호 검증 (암호화된 DB 비번과 사용자가 친 비번이 맞는지 확인)
        if (!passwordEncoder.matches(rawPassword, member.getPassword())) {
            throw new RuntimeException("비밀번호가 틀렸습니다.");
        }

        // 3. 성공 시 JWT 토큰 발급
        String token = jwtUtil.generateToken(member.getEmail(), member.getRole());

        // 프론트엔드에게 토큰과 이름을 넘겨줍니다.
        return Map.of(
                "token", token,
                "name", member.getName()
        );
    }
}
