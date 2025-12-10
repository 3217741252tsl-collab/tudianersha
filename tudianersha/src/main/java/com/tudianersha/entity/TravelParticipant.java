package com.tudianersha.entity;

import javax.persistence.*;

@Entity
@Table(name = "travel_participants")
public class TravelParticipant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "project_id", nullable = false)
    private Long projectId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "permission", nullable = false)
    private String permission; // 创建者/编辑者/查看者
    
    // Constructors
    public TravelParticipant() {}
    
    public TravelParticipant(Long projectId, Long userId, String permission) {
        this.projectId = projectId;
        this.userId = userId;
        this.permission = permission;
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
    
    public String getPermission() {
        return permission;
    }
    
    public void setPermission(String permission) {
        this.permission = permission;
    }
    
    @Override
    public String toString() {
        return "TravelParticipant{" +
                "id=" + id +
                ", projectId=" + projectId +
                ", userId=" + userId +
                ", permission='" + permission + '\'' +
                '}';
    }
}