package com.banjangNote.banjangnote_api.repository;

import com.banjangNote.banjangnote_api.entity.ProjectMaterial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectMaterialRepository extends JpaRepository<ProjectMaterial, Long> {
    // 특정 현장의 부자재 내역을 ID순으로 가져오기
    List<ProjectMaterial> findByProjectIdOrderByIdAsc(Long projectId);
}
