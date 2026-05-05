package com.banjangNote.banjangnote_api.controller;

import com.banjangNote.banjangnote_api.entity.Assignment;
import com.banjangNote.banjangnote_api.repository.AssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class AssignmentController {

    private final AssignmentRepository assignmentRepository;

    // GET: /api/projects/{projectId}/assignments (특정 현장의 작업자 리스트 조회)
    @GetMapping("/projects/{projectId}/assignments")
    public List<Assignment> getAssignmentsByProject(@PathVariable Long projectId) {
        return assignmentRepository.findByProjectId(projectId);
    }

    // PUT: /api/assignments/{id} (공수 증감 및 지급 여부 업데이트)
    @PutMapping("/assignments/{id}")
    public Assignment updateAssignment(@PathVariable Long id, @RequestBody Assignment updatedData) {
        // 1. 기존 데이터 찾기
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 정산 내역이 없습니다. id=" + id));

        // 2. 데이터 업데이트 (프론트에서 넘어온 days와 isPaid 값으로 덮어씌움)
        assignment.setDays(updatedData.getDays());
        assignment.setIsPaid(updatedData.getIsPaid());

        // 3. DB에 저장 후 결과 반환
        return assignmentRepository.save(assignment);
    }
}