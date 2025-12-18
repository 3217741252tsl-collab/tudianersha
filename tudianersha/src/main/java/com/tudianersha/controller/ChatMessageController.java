package com.tudianersha.controller;

import com.tudianersha.entity.ChatMessage;
import com.tudianersha.service.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat-messages")
public class ChatMessageController {
    
    @Autowired
    private ChatMessageService chatMessageService;
    
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<ChatMessage>> getMessagesByProject(@PathVariable Long projectId) {
        List<ChatMessage> messages = chatMessageService.getMessagesByProjectId(projectId);
        return new ResponseEntity<>(messages, HttpStatus.OK);
    }
    
    @GetMapping("/project/{projectId}/new")
    public ResponseEntity<List<ChatMessage>> getNewMessages(
            @PathVariable Long projectId,
            @RequestParam Long lastMessageId) {
        List<ChatMessage> messages = chatMessageService.getNewMessages(projectId, lastMessageId);
        return new ResponseEntity<>(messages, HttpStatus.OK);
    }
    
    @PostMapping
    public ResponseEntity<ChatMessage> sendMessage(@RequestBody ChatMessage message) {
        ChatMessage savedMessage = chatMessageService.saveMessage(message);
        return new ResponseEntity<>(savedMessage, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ChatMessage> getMessageById(@PathVariable Long id) {
        ChatMessage message = chatMessageService.getMessageById(id);
        if (message != null) {
            return new ResponseEntity<>(message, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ChatMessage> updateMessage(@PathVariable Long id, @RequestBody ChatMessage messageDetails) {
        ChatMessage existingMessage = chatMessageService.getMessageById(id);
        if (existingMessage == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        existingMessage.setMessage(messageDetails.getMessage());
        ChatMessage updated = chatMessageService.saveMessage(existingMessage);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteMessage(@PathVariable Long id) {
        chatMessageService.deleteMessage(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
