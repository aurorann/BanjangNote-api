package com.banjangNote.banjangnote_api.service;

import com.banjangNote.banjangnote_api.entity.Material;
import com.banjangNote.banjangnote_api.entity.Project;
import com.banjangNote.banjangnote_api.entity.ProjectMaterial;
import com.banjangNote.banjangnote_api.repository.MaterialRepository;
import com.banjangNote.banjangnote_api.repository.ProjectMaterialRepository;
import com.banjangNote.banjangnote_api.repository.ProjectRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final ProjectMaterialRepository projectMaterialRepository;
    private final ProjectRepository projectRepository;

    /**
     * 신규 부자재를 프로젝트에 추가
     */
    public ProjectMaterial createProjectMaterial(Long projectId, Map<String, Object> payload) {
        // 1. 프로젝트 존재 여부 확인
        Project project = projectRepository.findById(projectId)
                                           .orElseThrow(() -> new IllegalArgumentException("해당 현장이 없습니다. id=" + projectId));

        // 2. Payload에서 데이터 추출
        String name = (String) payload.get("name");
        Integer unitPrice = (Integer) payload.get("unitPrice");
        Integer quantity = (Integer) payload.get("quantity");

        // 3. 공통 부자재(Material) 조회 또는 생성
        Material material = findOrCreateMaterial(name, unitPrice);

        // 4. 프로젝트-부자재 연결 내역(ProjectMaterial) 생성 및 저장
        ProjectMaterial newProjectMaterial = new ProjectMaterial();
        newProjectMaterial.setProjectId(project.getId());
        newProjectMaterial.setMaterial(material);
        newProjectMaterial.setQuantity(quantity);

        return projectMaterialRepository.save(newProjectMaterial);
    }

    /**
     * 기존 프로젝트 부자재 내역 상세 수정
     */
    public ProjectMaterial updateProjectMaterialDetail(Long projectMaterialId, Map<String, Object> payload) {
        // 1. 수정할 프로젝트-부자재 내역 조회
        ProjectMaterial pm = projectMaterialRepository.findById(projectMaterialId)
                                                      .orElseThrow(() -> new IllegalArgumentException("해당 부자재 내역이 없습니다. id=" + projectMaterialId));

        // 2. Payload에서 데이터 추출
        String name = (String) payload.get("name");
        Integer unitPrice = (Integer) payload.get("unitPrice");
        Integer quantity = (Integer) payload.get("quantity");

        // 3. 새로운 정보로 공통 부자재(Material) 조회 또는 생성
        Material material = findOrCreateMaterial(name, unitPrice);

        // 4. 프로젝트-부자재 내역 업데이트 (JPA의 Dirty Checking 활용)
        pm.setMaterial(material); // 부자재 마스터 정보 변경
        pm.setQuantity(quantity); // 수량 변경

        return pm; // @Transactional에 의해 변경된 내용이 자동으로 DB에 반영됩니다.
    }

    /**
     * 공통 로직: 이름과 단가로 Material을 찾고, 없으면 새로 생성하여 반환
     */
    private Material findOrCreateMaterial(String name, Integer unitPrice) {
        // 이름과 단가로 기존 부자재를 찾고, 없으면 새로 생성(orElseGet)
        return materialRepository.findByNameAndUnitPrice(name, unitPrice)
                                 .orElseGet(() -> {
                                     Material newMaterial = new Material();
                                     newMaterial.setName(name);
                                     newMaterial.setUnitPrice(unitPrice);
                                     return materialRepository.save(newMaterial);
                                 });
    }
}
