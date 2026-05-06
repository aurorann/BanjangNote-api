package com.banjangNote.banjangnote_api.repository;

import com.banjangNote.banjangnote_api.entity.Client;
import com.banjangNote.banjangnote_api.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findByMember(Member member);
}
