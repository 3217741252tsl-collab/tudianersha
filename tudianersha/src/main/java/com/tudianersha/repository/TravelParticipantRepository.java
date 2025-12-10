package com.tudianersha.repository;

import com.tudianersha.entity.TravelParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TravelParticipantRepository extends JpaRepository<TravelParticipant, Long> {
    List<TravelParticipant> findByProjectId(Long projectId);
    List<TravelParticipant> findByUserId(Long userId);
    Optional<TravelParticipant> findByProjectIdAndUserId(Long projectId, Long userId);
    Boolean existsByProjectIdAndUserId(Long projectId, Long userId);
    Boolean existsByProjectIdAndPermission(Long projectId, String permission);
}