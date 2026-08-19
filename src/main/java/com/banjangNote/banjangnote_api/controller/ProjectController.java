package com.banjangNote.banjangnote_api.controller;

import com.banjangNote.banjangnote_api.entity.*;
import com.banjangNote.banjangnote_api.repository.*;
import com.banjangNote.banjangnote_api.service.AuthService;
import com.banjangNote.banjangnote_api.service.MaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectRepository projectRepository;
    private final ClientRepository clientRepository;
    private final AssignmentRepository assignmentRepository;
    private final WorkerRepository workerRepository;
    private final AuthService authService;
    private final MaterialService materialService;

    @GetMapping("/{id}")
    public Project getProjectById(@PathVariable Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 현장이 없습니다. id=" + id));
    }

    @PostMapping
    public Project createProject(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> payload) {
        Member member = authService.getMemberFromHeader(authHeader);

        Project project = new Project();
        project.setMember(member);

        Project savedProject = saveProjectDetails(project, payload);

        if (payload.get("workers") != null) {
            List<Map<String, Object>> workerList = (List<Map<String, Object>>) payload.get("workers");
            saveWorkersToProject(savedProject, workerList);
        }

        return savedProject;
    }

    @GetMapping
    public Page<Project> getMyProjects(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        Member member = authService.getMemberFromHeader(authHeader);

        // 프론트에서 온 날짜 문자열(String)을 DB가 이해할 수 있는 날짜 객체(LocalDate)로 변환
        LocalDate parsedStartDate = (startDate != null && !startDate.isEmpty())
                ? LocalDate.parse(startDate) : null;
        LocalDate parsedEndDate = (endDate != null && !endDate.isEmpty())
                ? LocalDate.parse(endDate) : null;

        // Pageable 객체 생성
        Pageable pageable = PageRequest.of(page, size);

        return projectRepository.findFilteredProjects(member, clientId, parsedStartDate, parsedEndDate, pageable);
    }

    @PutMapping("/{id}")
    public Project updateProject(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> payload) {

        Member member = authService.getMemberFromHeader(authHeader);
        Project existingProject = projectRepository.findById(id)
                                                   .orElseThrow(() -> new RuntimeException("해당 현장을 찾을 수 없습니다."));

        // 권한 체크: DB에 저장된 현장의 주인 ID와, 지금 요청한 사람의 ID가 같은지 확인
        if (!existingProject.getMember().getId().equals(member.getId())) {
            throw new RuntimeException("이 현장을 수정할 권한이 없습니다.");
        }

        // 권한이 확인되면 기존 로직 수행
        Project updatedProject = saveProjectDetails(existingProject, payload);
        saveWorkersToProject(updatedProject, (List<Map<String, Object>>) payload.get("workers"));

        return updatedProject;
    }

    // 현장 삭제 (내 현장만 삭제 가능)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProject(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        Member member = authService.getMemberFromHeader(authHeader);
        Project existingProject = projectRepository.findById(id)
                                                   .orElseThrow(() -> new RuntimeException("해당 현장을 찾을 수 없습니다."));

        // 권한 체크: 내 현장이 맞는지 확인
        if (!existingProject.getMember().getId().equals(member.getId())) {
            throw new RuntimeException("이 현장을 삭제할 권한이 없습니다.");
        }

        projectRepository.delete(existingProject);

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

    private void saveWorkersToProject(Project project, List<Map<String, Object>> workers) {
        // 기존에 이 현장에 배정된 작업자 내역 조회
        List<Assignment> existingAssignments = assignmentRepository.findByProjectId(project.getId());

        // 프론트에서 작업자를 모두 삭제해서 보낸 경우, 기존 배정 내역을 전부 삭제하고 종료
        if (workers == null || workers.isEmpty()) {
            assignmentRepository.deleteAll(existingAssignments);
            return;
        }

        // 이번 요청으로 들어온(유지되거나 새로 추가된) 작업자의 ID를 추적할 리스트
        List<Long> requestedWorkerIds = new ArrayList<>();

        for (Map<String, Object> w : workers) {
            String workerName = w.get("name").toString();
            if (workerName.trim().isEmpty()) continue;

            int dailyRate = Integer.parseInt(w.get("dailyRate").toString());
            String role = w.get("role") != null ? w.get("role").toString() : "";

            // Worker 정보 확인 및 업데이트 (기존 작업자면 정보 갱신, 없으면 신규 생성)
            Worker worker = workerRepository.findByName(workerName).orElse(new Worker());
            worker.setName(workerName);
            worker.setRole(role);
            worker.setDefaultDailyRate(dailyRate);

            // save한 결과를 새로운 변수(savedWorker)에 담기
            Worker savedWorker = workerRepository.save(worker);

            // 람다식에서 안전하게 쓸 수 있도록 ID를 별도의 변수(workerId)로 빼둠
            Long workerId = savedWorker.getId();

            requestedWorkerIds.add(workerId);

            // 이 현장에 이미 배정된 작업자인지 확인
            Optional<Assignment> existingAssignmentOpt = existingAssignments.stream()
                                                                            // worker.getId() 대신, 값이 변하지 않는 workerId를 사용
                                                                            .filter(a -> a.getWorker().getId().equals(workerId))
                                                                            .findFirst();

            if (existingAssignmentOpt.isPresent()) {
                Assignment assignment = existingAssignmentOpt.get();
                assignment.setAppliedDailyRate(dailyRate);
                assignmentRepository.save(assignment);
            } else {
                Assignment newAssignment = new Assignment();
                newAssignment.setProject(project);
                // 새 배정 객체에도 savedWorker 넣어줌
                newAssignment.setWorker(savedWorker);
                newAssignment.setAppliedDailyRate(dailyRate);
                assignmentRepository.save(newAssignment);
            }
        }

        // 삭제 처리: 기존 현장 배정 내역 중, 이번 요청(workers)에 포함되지 않은 작업자는 현장에서 제외(삭제)
        List<Assignment> assignmentsToRemove = existingAssignments.stream()
                                                                  .filter(a -> !requestedWorkerIds.contains(a.getWorker().getId()))
                                                                  .collect(Collectors.toList());

        if (!assignmentsToRemove.isEmpty()) {
            assignmentRepository.deleteAll(assignmentsToRemove);
        }
    }

    // 특정 필드(수금 상태)만 업데이트하는 전용 엔드포인트
    @PatchMapping("/{id}/settle")
    public ResponseEntity<String> toggleSettleStatus(@PathVariable Long id) {
        Project project = projectRepository.findById(id)
                                           .orElseThrow(() -> new IllegalArgumentException("현장이 없습니다."));

        // 현재 상태를 반전 (true -> false, false -> true)
        project.setIsSettled(!project.getIsSettled());
        projectRepository.save(project);

        return ResponseEntity.ok("수금 상태가 변경되었습니다.");
    }


    /**
     * API: POST /projects/{id}/materials
     * 기능: 특정 프로젝트에 신규 부자재 내역을 추가합니다.
     */
    @PostMapping("/{id}/materials")
    public ProjectMaterial createProjectMaterials(@PathVariable Long id,
                                                  @RequestBody Map<String, Object> payload) {
        // 권한 검증이 필요하다면 여기서 authService 등을 활용
        return materialService.createProjectMaterial(id, payload);
    }
}
