package com.tudianersha.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "project_participants")
public class ProjectParticipant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "project_id", nullable = false)
    private Long projectId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "join_time", nullable = false)
    private LocalDateTime joinTime;
    
    @Column(name = "role", nullable = false)
    private String role; // 创建者/编辑者/查看者
    
    // Constructors
    public ProjectParticipant() {}
    
    public ProjectParticipant(Long projectId, Long userId, LocalDateTime joinTime, String role) {
        this.projectId = projectId;
        this.userId = userId;
        this.joinTime = joinTime;
        this.role = role;
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
    
    public LocalDateTime getJoinTime() {
        return joinTime;
    }
    
    public void setJoinTime(LocalDateTime joinTime) {
        this.joinTime = joinTime;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    @Override
    public String toString() {
        return "ProjectParticipant{" +
                "id=" + id +
                ", projectId=" + projectId +
                ", userId=" + userId +
                ", joinTime=" + joinTime +
                ", role='" + role + '\'' +
                '}';
    }
}