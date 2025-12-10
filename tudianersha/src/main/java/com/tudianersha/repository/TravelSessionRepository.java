package com.tudianersha.repository;

import com.tudianersha.entity.TravelSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TravelSessionRepository extends JpaRepository<TravelSession, Long> {
    List<TravelSession> findByProjectIdOrderByMessageTimeAsc(Long projectId);
    List<TravelSession> findByProjectIdAndUserId(Long projectId, Long userId);
}