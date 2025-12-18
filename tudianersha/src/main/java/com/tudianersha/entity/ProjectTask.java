package com.tudianersha.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "project_tasks")
public class ProjectTask {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "project_id", nullable = false)
    private Long projectId;
    
    @Column(name = "task_name", nullable = false, length = 200)
    private String taskName;
    
    @Column(name = "assignee_id", nullable = false)
    private Long assigneeId;
    
    @Column(name = "created_by", nullable = false)
    private Long createdBy;
    
    @Column(name = "status", length = 20)
    private String status = "PENDING";
    
    @Column(name = "image_urls", columnDefinition = "TEXT")
    private String imageUrls;
    
    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;
    
    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;
    
    // Transient field for assignee info
    @Transient
    private User assigneeInfo;

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

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(String imageUrls) {
        this.imageUrls = imageUrls;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }

    public User getAssigneeInfo() {
        return assigneeInfo;
    }

    public void setAssigneeInfo(User assigneeInfo) {
        this.assigneeInfo = assigneeInfo;
    }
}
