package com.tudianersha.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity_schedules")
public class ActivitySchedule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "project_id", nullable = false)
    private Long projectId;
    
    @Column(name = "activity_name", nullable = false)
    private String activityName;
    
    @Column(name = "activity_time")
    private LocalDateTime activityTime;
    
    @Column(name = "location")
    private String location;
    
    @Column(name = "budget", precision = 10, scale = 2)
    private BigDecimal budget;
    
    @Column(name = "day_number")
    private Integer dayNumber;
    
    // Constructors
    public ActivitySchedule() {}
    
    public ActivitySchedule(Long projectId, String activityName, LocalDateTime activityTime, 
                           String location, BigDecimal budget, Integer dayNumber) {
        this.projectId = projectId;
        this.activityName = activityName;
        this.activityTime = activityTime;
        this.location = location;
        this.budget = budget;
        this.dayNumber = dayNumber;
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
    
    public String getActivityName() {
        return activityName;
    }
    
    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }
    
    public LocalDateTime getActivityTime() {
        return activityTime;
    }
    
    public void setActivityTime(LocalDateTime activityTime) {
        this.activityTime = activityTime;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public BigDecimal getBudget() {
        return budget;
    }
    
    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }
    
    public Integer getDayNumber() {
        return dayNumber;
    }
    
    public void setDayNumber(Integer dayNumber) {
        this.dayNumber = dayNumber;
    }
    
    @Override
    public String toString() {
        return "ActivitySchedule{" +
                "id=" + id +
                ", projectId=" + projectId +
                ", activityName='" + activityName + '\'' +
                ", activityTime=" + activityTime +
                ", location='" + location + '\'' +
                ", budget=" + budget +
                ", dayNumber=" + dayNumber +
                '}';
    }
}