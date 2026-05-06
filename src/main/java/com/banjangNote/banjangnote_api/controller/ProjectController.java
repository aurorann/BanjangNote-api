package com.banjangNote.banjangnote_api.controller;

import com.banjangNote.banjangnote_api.entity.*;
import com.banjangNote.banjangnote_api.repository.*;
import com.banjangNote.banjangnote_api.service.AuthService;
import com.banjangNote.banjangnote_api.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectRepository projectRepository;
    private final ClientRepository clientRepository;
    private final AssignmentRepository assignmentRepository;
    private final WorkerRepository workerRepository;
    private final AuthService authService;

    @GetMapping("/{id}")
    public Project getProjectById(@PathVariable Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 현장이 없습니다. id=" + id));
    }

    @PostMapping
    public Project createProject(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Project project) {

        Member member = authService.getMemberFromHeader(authHeader);
        project.setMember(member);

        return projectRepository.save(project);
    }

    @GetMapping
    public List<Project> getMyProjects(@RequestHeader("Authorization") String authHeader) {

        Member member = authService.getMemberFromHeader(authHeader);
        return projectRepository.findByMember(member); // 내 현장만 가져오기
    }

    @PutMapping("/{id}")
    public Project updateProject(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader, // 🔥 토큰 받기
            @RequestBody Map<String, Object> payload) {

        Member member = authService.getMemberFromHeader(authHeader);
        Project existingProject = projectRepository.findById(id)
                                                   .orElseThrow(() -> new RuntimeException("해당 현장을 찾을 수 없습니다."));

        // 🚨 권한 체크: DB에 저장된 현장의 주인 ID와, 지금 요청한 사람의 ID가 같은지 확인!
        if (!existingProject.getMember().getId().equals(member.getId())) {
            throw new RuntimeException("이 현장을 수정할 권한이 없습니다.");
        }

        // 권한이 확인되면 기존 로직 수행
        Project updatedProject = saveProjectDetails(existingProject, payload);
        saveWorkersToProject(updatedProject, (List<Map<String, Object>>) payload.get("workers"));

        return updatedProject;
    }

    // 🗑️ 4. 현장 삭제 (내 현장만 삭제 가능)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProject(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) { // 🔥 토큰 받기

        Member member = authService.getMemberFromHeader(authHeader);
        Project existingProject = projectRepository.findById(id)
                                                   .orElseThrow(() -> new RuntimeException("해당 현장을 찾을 수 없습니다."));

        // 🚨 권한 체크: 내 현장이 맞는지 확인!
        if (!existingProject.getMember().getId().equals(member.getId())) {
            throw new RuntimeException("이 현장을 삭제할 권한이 없습니다.");
        }

        projectRepository.delete(existingProject); // 완벽하게 삭제

        // 삭제 성공 메시지 반환 (프론트에서 읽을 수 있게 ResponseEntity 사용)
        return ResponseEntity.ok("현장이 안전하게 삭제되었습니다.");
    }

    // --- 내부 공통 로직 (현장 정보 저장) ---
    private Project saveProjectDetails(Project project, Map<String, Object> payload) {
        project.setName(payload.get("name").toString());
        project.setAddress(payload.get("address") != null ? payload.get("address").toString() : "");

        if (payload.get("startDate") != null && !payload.get("startDate").toString().isEmpty()) {
            project.setStartDate(java.time.LocalDate.parse(payload.get("startDate").toString()));
        }
        if (payload.get("endDate") != null && !payload.get("endDate").toString().isEmpty()) {
            project.setEndDate(java.time.LocalDate.parse(payload.get("endDate").toString()));
        }
        if (payload.get("clientId") != null && !payload.get("clientId").toString().isEmpty()) {
            Long clientId = Long.valueOf(payload.get("clientId").toString());
            project.setClient(clientRepository.findById(clientId).orElse(null));
        }
        if (payload.get("isSettled") != null) {
            project.setIsSettled(Boolean.parseBoolean(payload.get("isSettled").toString()));
        }
        return projectRepository.save(project);
    }

    // --- 내부 공통 로직 (작업자 투입 내역 저장) ---
    private void saveWorkersToProject(Project project, List<Map<String, Object>> workers) {
        if (workers == null || workers.isEmpty()) return;

        for (Map<String, Object> w : workers) {
            String workerName = w.get("name").toString();
            if (workerName.trim().isEmpty()) continue;

            // 1) 동명이인이 있는지 찾아보고, 없으면 새 작업자로 인력풀에 등록!
            Worker worker = workerRepository.findByName(workerName).orElseGet(() -> {
                Worker newWorker = new Worker();
                newWorker.setName(workerName);
                newWorker.setRole(w.get("role") != null ? w.get("role").toString() : "");
                newWorker.setDefaultDailyRate(Integer.parseInt(w.get("dailyRate").toString()));
                return workerRepository.save(newWorker);
            });

            // 2) 이 현장에 이미 투입된 사람인지 확인 (중복 등록 방지)
            boolean alreadyAssigned = assignmentRepository.findByProjectId(project.getId())
                    .stream().anyMatch(a -> a.getWorker().getId().equals(worker.getId()));

            if (!alreadyAssigned) {
                Assignment assignment = new Assignment();
                assignment.setProject(project);
                assignment.setWorker(worker);
                assignment.setAppliedDailyRate(Integer.parseInt(w.get("dailyRate").toString()));
                assignmentRepository.save(assignment);
            }
        }
    }
}
