package com.tudianersha.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 交通卡片实体
 * 用于协作界面景点之间的交通信息管理
 */
@Entity
@Table(name = "transport_cards")
public class TransportCard {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "project_id", nullable = false)
    private Long projectId;
    
    @Column(name = "day_number", nullable = false)
    private Integer dayNumber;
    
    @Column(name = "from_activity_id", nullable = false)
    private String fromActivityId;
    
    @Column(name = "to_activity_id", nullable = false)
    private String toActivityId;
    
    @Column(name = "from_name")
    private String fromName;
    
    @Column(name = "to_name")
    private String toName;
    
    @Column(name = "from_location")
    private String fromLocation; // 经纬度 "lng,lat"
    
    @Column(name = "to_location")
    private String toLocation;
    
    @Column(name = "status", nullable = false)
    private String status; // "pending"(待定) 或 "confirmed"(已确认)
    
    @Column(name = "method")
    private String method; // 交通方式简述，如"地铁3号线"
    
    @Column(name = "distance")
    private Integer distance; // 距离（米）
    
    @Column(name = "distance_text")
    private String distanceText; // "8.5公里"
    
    @Column(name = "duration")
    private Integer duration; // 时长（秒）
    
    @Column(name = "duration_text")
    private String durationText; // "25分钟"
    
    @Column(name = "cost")
    private Double cost; // 费用（元）
    
    @Column(name = "cost_text")
    private String costText; // "¥4"
    
    @Column(name = "steps_json", columnDefinition = "TEXT")
    private String stepsJson; // 详细步骤（JSON格式）
    
    @Column(name = "alternative_plans", columnDefinition = "TEXT")
    private String alternativePlans; // 备选方案（JSON数组）
    
    @Column(name = "selected_plan_index")
    private Integer selectedPlanIndex; // 当前选中的方案索引，默认0
    
    @Column(name = "created_time")
    private LocalDateTime createdTime;
    
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    @Column(name = "updated_by")
    private Long updatedBy;
    
    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
        updatedTime = LocalDateTime.now();
        if (selectedPlanIndex == null) {
            selectedPlanIndex = 0;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
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
    
    public Integer getDayNumber() {
        return dayNumber;
    }
    
    public void setDayNumber(Integer dayNumber) {
        this.dayNumber = dayNumber;
    }
    
    public String getFromActivityId() {
        return fromActivityId;
    }
    
    public void setFromActivityId(String fromActivityId) {
        this.fromActivityId = fromActivityId;
    }
    
    public String getToActivityId() {
        return toActivityId;
    }
    
    public void setToActivityId(String toActivityId) {
        this.toActivityId = toActivityId;
    }
    
    public String getFromName() {
        return fromName;
    }
    
    public void setFromName(String fromName) {
        this.fromName = fromName;
    }
    
    public String getToName() {
        return toName;
    }
    
    public void setToName(String toName) {
        this.toName = toName;
    }
    
    public String getFromLocation() {
        return fromLocation;
    }
    
    public void setFromLocation(String fromLocation) {
        this.fromLocation = fromLocation;
    }
    
    public String getToLocation() {
        return toLocation;
    }
    
    public void setToLocation(String toLocation) {
        this.toLocation = toLocation;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getMethod() {
        return method;
    }
    
    public void setMethod(String method) {
        this.method = method;
    }
    
    public Integer getDistance() {
        return distance;
    }
    
    public void setDistance(Integer distance) {
        this.distance = distance;
    }
    
    public String getDistanceText() {
        return distanceText;
    }
    
    public void setDistanceText(String distanceText) {
        this.distanceText = distanceText;
    }
    
    public Integer getDuration() {
        return duration;
    }
    
    public void setDuration(Integer duration) {
        this.duration = duration;
    }
    
    public String getDurationText() {
        return durationText;
    }
    
    public void setDurationText(String durationText) {
        this.durationText = durationText;
    }
    
    public Double getCost() {
        return cost;
    }
    
    public void setCost(Double cost) {
        this.cost = cost;
    }
    
    public String getCostText() {
        return costText;
    }
    
    public void setCostText(String costText) {
        this.costText = costText;
    }
    
    public String getStepsJson() {
        return stepsJson;
    }
    
    public void setStepsJson(String stepsJson) {
        this.stepsJson = stepsJson;
    }
    
    public String getAlternativePlans() {
        return alternativePlans;
    }
    
    public void setAlternativePlans(String alternativePlans) {
        this.alternativePlans = alternativePlans;
    }
    
    public Integer getSelectedPlanIndex() {
        return selectedPlanIndex;
    }
    
    public void setSelectedPlanIndex(Integer selectedPlanIndex) {
        this.selectedPlanIndex = selectedPlanIndex;
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
    
    public Long getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }
    
    public Long getUpdatedBy() {
        return updatedBy;
    }
    
    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }
}
