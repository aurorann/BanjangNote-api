package com.banjangNote.banjangnote_api.repository;

import com.banjangNote.banjangnote_api.entity.Worker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkerRepository extends JpaRepository<Worker, Long>{
    Optional<Worker> findByName(String name);
}
