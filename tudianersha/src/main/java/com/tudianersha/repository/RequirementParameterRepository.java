package com.tudianersha.repository;

import com.tudianersha.entity.RequirementParameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequirementParameterRepository extends JpaRepository<RequirementParameter, Long> {
    Optional<RequirementParameter> findByProjectId(Long projectId);
    List<RequirementParameter> findAllByProjectId(Long projectId);
}