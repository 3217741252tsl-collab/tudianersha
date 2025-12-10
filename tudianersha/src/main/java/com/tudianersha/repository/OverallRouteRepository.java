package com.tudianersha.repository;

import com.tudianersha.entity.OverallRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OverallRouteRepository extends JpaRepository<OverallRoute, Long> {
    List<OverallRoute> findByProjectId(Long projectId);
}