package com.tudianersha.repository;

import com.tudianersha.entity.ActivitySchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityScheduleRepository extends JpaRepository<ActivitySchedule, Long> {
    List<ActivitySchedule> findByProjectId(Long projectId);
    List<ActivitySchedule> findByProjectIdOrderByDayNumberAscActivityTimeAsc(Long projectId);
}