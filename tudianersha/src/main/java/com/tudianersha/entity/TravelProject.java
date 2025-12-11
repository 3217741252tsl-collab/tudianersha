package com.tudianersha.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "travel_projects")
public class TravelProject {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "project_name", nullable = false)
    private String projectName;
    
    @Column(name = "destination", nullable = false)
    private String destination;
    
    @Column(name = "days", nullable = false)
    private Integer days;
    
    @Column(name = "creator_id", nullable = false)
    private Long creatorId;
    
    @Column(name = "status", nullable = false)
    private String status; // 草稿/规划中/已归档
    
    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;
    
    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;
    
    @Column(name = "current_route_id")
    private Long currentRouteId; // 当前方案ID
    
    @Column(name = "start_date")
    private String startDate; // 开始日期
    
    @Column(name = "end_date")
    private String endDate; // 结束日期
    
    @Column(name = "total_budget")
    private Double totalBudget; // 总预算
    
    // Constructors
    public TravelProject() {}
    
    public TravelProject(String projectName, String destination, Integer days, Long creatorId, 
                        String status, LocalDateTime createdTime, LocalDateTime updatedTime) {
        this.projectName = projectName;
        this.destination = destination;
        this.days = days;
        this.creatorId = creatorId;
        this.status = status;
        this.createdTime = createdTime;
        this.updatedTime = updatedTime;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getProjectName() {
        return projectName;
    }
    
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    
    public String getDestination() {
        return destination;
    }
    
    public void setDestination(String destination) {
        this.destination = destination;
    }
    
    public Integer getDays() {
        return days;
    }
    
    public void setDays(Integer days) {
        this.days = days;
    }
    
    public Long getCreatorId() {
        return creatorId;
    }
    
    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
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
    
    public Long getCurrentRouteId() {
        return currentRouteId;
    }
    
    public void setCurrentRouteId(Long currentRouteId) {
        this.currentRouteId = currentRouteId;
    }
    
    public String getStartDate() {
        return startDate;
    }
    
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
    
    public String getEndDate() {
        return endDate;
    }
    
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
    
    public Double getTotalBudget() {
        return totalBudget;
    }
    
    public void setTotalBudget(Double totalBudget) {
        this.totalBudget = totalBudget;
    }
    
    @Override
    public String toString() {
        return "TravelProject{" +
                "id=" + id +
                ", projectName='" + projectName + '\'' +
                ", destination='" + destination + '\'' +
                ", days=" + days +
                ", creatorId=" + creatorId +
                ", status='" + status + '\'' +
                ", createdTime=" + createdTime +
                ", updatedTime=" + updatedTime +
                ", currentRouteId=" + currentRouteId +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                '}';
    }
}