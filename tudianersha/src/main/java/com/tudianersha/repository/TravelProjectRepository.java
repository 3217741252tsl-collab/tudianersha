package com.tudianersha.repository;

import com.tudianersha.entity.TravelProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TravelProjectRepository extends JpaRepository<TravelProject, Long> {
    List<TravelProject> findByCreatorId(Long creatorId);
    List<TravelProject> findByStatus(String status);
}