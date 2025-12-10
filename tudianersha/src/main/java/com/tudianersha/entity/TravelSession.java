package com.tudianersha.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "travel_sessions")
public class TravelSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "project_id", nullable = false)
    private Long projectId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;
    
    @Column(name = "message_time", nullable = false)
    private LocalDateTime messageTime;
    
    @Column(name = "mentioned_user_id")
    private Long mentionedUserId; // @用户ID
    
    // Constructors
    public TravelSession() {}
    
    public TravelSession(Long projectId, Long userId, String message, LocalDateTime messageTime, Long mentionedUserId) {
        this.projectId = projectId;
        this.userId = userId;
        this.message = message;
        this.messageTime = messageTime;
        this.mentionedUserId = mentionedUserId;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getProjectId() {
        return projectId;
    }
    
    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public LocalDateTime getMessageTime() {
        return messageTime;
    }
    
    public void setMessageTime(LocalDateTime messageTime) {
        this.messageTime = messageTime;
    }
    
    public Long getMentionedUserId() {
        return mentionedUserId;
    }
    
    public void setMentionedUserId(Long mentionedUserId) {
        this.mentionedUserId = mentionedUserId;
    }
    
    @Override
    public String toString() {
        return "TravelSession{" +
                "id=" + id +
                ", projectId=" + projectId +
                ", userId=" + userId +
                ", message='" + message + '\'' +
                ", messageTime=" + messageTime +
                ", mentionedUserId=" + mentionedUserId +
                '}';
    }
}