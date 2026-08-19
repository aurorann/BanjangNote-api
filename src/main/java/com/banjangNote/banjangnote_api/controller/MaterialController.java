package com.banjangNote.banjangnote_api.controller;

import com.banjangNote.banjangnote_api.entity.ProjectMaterial;
import com.banjangNote.banjangnote_api.repository.ProjectMaterialRepository;
import com.banjangNote.banjangnote_api.service.MaterialService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MaterialController {
    private final ProjectMaterialRepository repository;
    private final MaterialService materialService;

    // 1. 부자재 조회 (GET /projects/:id/materials)
    @GetMapping("/projects/{projectId}/materials")
    public List<ProjectMaterial> getProjectMaterials(@PathVariable Long projectId) {
        return repository.findByProjectIdOrderByIdAsc(projectId);
    }

    // 2. 부자재 수량 업데이트 (PUT /materials/:id)
    @PutMapping("/materials/{id}")
    public ProjectMaterial updateMaterialQuantity(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> payload) { // 프론트에서 { "quantity": 10 } 형태로 보내므로 Map으로 받습니다.

        ProjectMaterial pm = repository.findById(id)
                                       .orElseThrow(() -> new IllegalArgumentException("해당 부자재 내역이 없습니다. id=" + id));

        Integer newQuantity = payload.get("quantity");
        if (newQuantity == null) {
            throw new IllegalArgumentException("quantity 값이 필요합니다.");
        }
        pm.setQuantity(newQuantity);

        // @Transactional이 클래스 레벨에 없으므로, save를 명시적으로 호출해주는 것이 안전합니다.
        return repository.save(pm);
    }

    /**
     * 3. 부자재 정보 상세 수정 (PUT /materials/:id/detail)
     * 기능: 부자재 내역의 상세 정보(이름, 단가, 수량)를 한번에 수정합니다.
     * @param id 수정할 ProjectMaterial의 ID
     */
    @PutMapping("/materials/{id}/detail")
    public ProjectMaterial updateMaterialDetail(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        return materialService.updateProjectMaterialDetail(id, payload);
    }
}
