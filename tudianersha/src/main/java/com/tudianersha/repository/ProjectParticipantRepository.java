package com.tudianersha.repository;

import com.tudianersha.entity.ProjectParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectParticipantRepository extends JpaRepository<ProjectParticipant, Long> {
    List<ProjectParticipant> findByProjectId(Long projectId);
    List<ProjectParticipant> findByUserId(Long userId);
    Optional<ProjectParticipant> findByProjectIdAndUserId(Long projectId, Long userId);
    Boolean existsByProjectIdAndUserId(Long projectId, Long userId);
}