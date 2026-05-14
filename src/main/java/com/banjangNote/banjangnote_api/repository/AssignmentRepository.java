package com.banjangNote.banjangnote_api.repository;

import com.banjangNote.banjangnote_api.entity.Assignment;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByProjectId(Long clientId, Sort sort);
    List<Assignment> findByProjectId(Long clientId);
    boolean existsByWorkerId(Long workerId);
}
