package com.banjangNote.banjangnote_api.controller;

import com.banjangNote.banjangnote_api.entity.Assignment;
import com.banjangNote.banjangnote_api.entity.Client;
import com.banjangNote.banjangnote_api.entity.Project;
import com.banjangNote.banjangnote_api.entity.Worker;
import com.banjangNote.banjangnote_api.repository.AssignmentRepository;
import com.banjangNote.banjangnote_api.repository.ClientRepository;
import com.banjangNote.banjangnote_api.repository.ProjectRepository;
import com.banjangNote.banjangnote_api.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
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

    // 1. 전체 현장 목록 조회
    @GetMapping
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    // 2. 특정 현장 1개 상세 조회 (현장 상세 화면 상단 정보용)
    @GetMapping("/{id}")
    public Project getProjectById(@PathVariable Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 현장이 없습니다. id=" + id));
    }

    // 🔥 1. 현장 및 작업자 최초 등록 (POST)
    @PostMapping
    public Project createProject(@RequestBody Map<String, Object> payload) {
        Project savedProject = saveProjectDetails(new Project(), payload);
        saveWorkersToProject(savedProject, (List<Map<String, Object>>) payload.get("workers"));
        return savedProject;
    }

    // 🔥 2. 현장 및 작업자 정보 수정 (PUT)
    @PutMapping("/{id}")
    public Project updateProject(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Project existingProject = projectRepository.findById(id).orElseThrow();
        Project updatedProject = saveProjectDetails(existingProject, payload);
        saveWorkersToProject(updatedProject, (List<Map<String, Object>>) payload.get("workers"));
        return updatedProject;
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
