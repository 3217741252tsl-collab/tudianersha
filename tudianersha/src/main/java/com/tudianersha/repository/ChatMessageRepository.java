package com.tudianersha.repository;

import com.tudianersha.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    List<ChatMessage> findByProjectIdOrderByCreatedTimeAsc(Long projectId);
    
    List<ChatMessage> findByProjectIdAndIdGreaterThanOrderByCreatedTimeAsc(Long projectId, Long lastMessageId);
}
