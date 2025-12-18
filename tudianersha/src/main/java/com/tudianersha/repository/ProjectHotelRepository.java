package com.tudianersha.repository;

import com.tudianersha.entity.ProjectHotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectHotelRepository extends JpaRepository<ProjectHotel, Long> {
    
    List<ProjectHotel> findByProjectIdOrderByDayIndex(Long projectId);
    
    Optional<ProjectHotel> findByProjectIdAndDayIndex(Long projectId, Integer dayIndex);
    
    void deleteByProjectId(Long projectId);
    
    boolean existsByProjectIdAndDayIndex(Long projectId, Integer dayIndex);
}
