package com.banjangNote.banjangnote_api.repository;

import com.banjangNote.banjangnote_api.entity.Member;
import com.banjangNote.banjangnote_api.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    // 🔥 Spring Data JPA의 마법! 이름만 이렇게 지어주면 알아서 WHERE member_id = ? 쿼리를 짜줍니다.
    List<Project> findByMember(Member member);
}
