package com.tudianersha.repository;

import com.tudianersha.entity.SharedDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SharedDocumentRepository extends JpaRepository<SharedDocument, Long> {
    List<SharedDocument> findByProjectId(Long projectId);
    List<SharedDocument> findByCreatorId(Long creatorId);
}