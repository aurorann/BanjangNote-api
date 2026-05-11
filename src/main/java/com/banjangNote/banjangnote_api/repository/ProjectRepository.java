package com.banjangNote.banjangnote_api.repository;

import com.banjangNote.banjangnote_api.entity.Member;
import com.banjangNote.banjangnote_api.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    @Query("SELECT p FROM Project p WHERE p.member = :member " +
            "AND (:clientId IS NULL OR p.client.id = :clientId) " +
            "AND (CAST(:startDate AS date) IS NULL OR p.startDate >= :startDate) " +
            "AND (CAST(:endDate AS date) IS NULL OR p.endDate <= :endDate) " +
            "ORDER BY p.startDate DESC")
    Page<Project> findFilteredProjects(
            @Param("member") Member member,
            @Param("clientId") Long clientId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );
}
