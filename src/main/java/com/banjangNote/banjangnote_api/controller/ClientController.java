package com.banjangNote.banjangnote_api.controller;

import com.banjangNote.banjangnote_api.entity.Client;
import com.banjangNote.banjangnote_api.entity.Member;
import com.banjangNote.banjangnote_api.repository.ClientRepository;
import com.banjangNote.banjangnote_api.repository.MemberRepository;
import com.banjangNote.banjangnote_api.service.AuthService;
import com.banjangNote.banjangnote_api.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientRepository clientRepository;
    private final AuthService authService;

    @GetMapping
    public List<Client> getAllClients(@RequestHeader("Authorization") String authHeader) {
        Member member = authService.getMemberFromHeader(authHeader);
        return clientRepository.findByMember(member);
    }

    @PostMapping
    public Client createClient(@RequestHeader("Authorization") String authHeader, @RequestBody Client client) {
        Member member = authService.getMemberFromHeader(authHeader);
        client.setMember(member);
        return clientRepository.save(client);
    }

    // ✏️ 3. 업체 정보 수정 (추가됨)
    @PutMapping("/{id}")
    public Client updateClient(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Client updatedClientData) {

        Member member = authService.getMemberFromHeader(authHeader);
        Client existingClient = clientRepository.findById(id)
                                                .orElseThrow(() -> new RuntimeException("해당 업체를 찾을 수 없습니다."));

        // 🚨 권한 체크: DB에 저장된 주인의 ID와 요청한 회원의 ID가 같은지 확인
        if (!existingClient.getMember().getId().equals(member.getId())) {
            throw new RuntimeException("이 업체를 수정할 권한이 없습니다.");
        }

        // 넘어온 데이터로 기존 데이터 업데이트
        existingClient.setName(updatedClientData.getName());
        existingClient.setContactName(updatedClientData.getContactName());
        existingClient.setContactPhone(updatedClientData.getContactPhone());

        return clientRepository.save(existingClient);
    }

    // 🗑️ 4. 업체 삭제 (추가됨)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteClient(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        Member member = authService.getMemberFromHeader(authHeader);
        Client existingClient = clientRepository.findById(id)
                                                .orElseThrow(() -> new RuntimeException("해당 업체를 찾을 수 없습니다."));

        // 🚨 권한 체크: 내 업체가 맞는지 확인
        if (!existingClient.getMember().getId().equals(member.getId())) {
            throw new RuntimeException("이 업체를 삭제할 권한이 없습니다.");
        }

        clientRepository.delete(existingClient);
        return ResponseEntity.ok("업체가 성공적으로 삭제되었습니다.");
    }
}
