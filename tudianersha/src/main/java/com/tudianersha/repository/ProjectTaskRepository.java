package com.tudianersha.repository;

import com.tudianersha.entity.ProjectTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectTaskRepository extends JpaRepository<ProjectTask, Long> {
    
    List<ProjectTask> findByProjectIdOrderByCreatedTimeDesc(Long projectId);
    
    List<ProjectTask> findByAssigneeId(Long assigneeId);
    
    List<ProjectTask> findByProjectIdAndAssigneeId(Long projectId, Long assigneeId);
}
