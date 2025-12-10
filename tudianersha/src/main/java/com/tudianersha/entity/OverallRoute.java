package com.tudianersha.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "overall_routes")
public class OverallRoute {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "project_id", nullable = false)
    private Long projectId;
    
    @Column(name = "route_details", columnDefinition = "TEXT")
    private String routeDetails;
    
    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;
    
    // Constructors
    public OverallRoute() {}
    
    public OverallRoute(Long projectId, String routeDetails, LocalDateTime createdTime) {
        this.projectId = projectId;
        this.routeDetails = routeDetails;
        this.createdTime = createdTime;
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
    
    public String getRouteDetails() {
        return routeDetails;
    }
    
    public void setRouteDetails(String routeDetails) {
        this.routeDetails = routeDetails;
    }
    
    public LocalDateTime getCreatedTime() {
        return createdTime;
    }
    
    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }
    
    @Override
    public String toString() {
        return "OverallRoute{" +
                "id=" + id +
                ", projectId=" + projectId +
                ", routeDetails='" + routeDetails + '\'' +
                ", createdTime=" + createdTime +
                '}';
    }
}