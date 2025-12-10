package com.tudianersha.repository;

import com.tudianersha.entity.AiGeneratedRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiGeneratedRouteRepository extends JpaRepository<AiGeneratedRoute, Long> {
    List<AiGeneratedRoute> findByProjectId(Long projectId);
}