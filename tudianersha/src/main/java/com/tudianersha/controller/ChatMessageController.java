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
    
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteMessage(@PathVariable Long id) {
        chatMessageService.deleteMessage(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
