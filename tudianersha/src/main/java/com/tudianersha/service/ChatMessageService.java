package com.tudianersha.service;

import com.tudianersha.entity.ChatMessage;
import com.tudianersha.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatMessageService {
    
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    
    public List<ChatMessage> getMessagesByProjectId(Long projectId) {
        return chatMessageRepository.findByProjectIdOrderByCreatedTimeAsc(projectId);
    }
    
    public List<ChatMessage> getNewMessages(Long projectId, Long lastMessageId) {
        return chatMessageRepository.findByProjectIdAndIdGreaterThanOrderByCreatedTimeAsc(projectId, lastMessageId);
    }
    
    public ChatMessage saveMessage(ChatMessage message) {
        return chatMessageRepository.save(message);
    }
    
    public void deleteMessage(Long id) {
        chatMessageRepository.deleteById(id);
    }
}
