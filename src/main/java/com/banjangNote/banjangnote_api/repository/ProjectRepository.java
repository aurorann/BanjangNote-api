package com.banjangNote.banjangnote_api.repository;

import com.banjangNote.banjangnote_api.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
