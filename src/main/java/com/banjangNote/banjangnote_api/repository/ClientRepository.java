package com.banjangNote.banjangnote_api.repository;

import com.banjangNote.banjangnote_api.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {
}
