package com.tudianersha.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_generated_routes")
public class AiGeneratedRoute {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "project_id", nullable = false)
    private Long projectId;
    
    @Column(name = "route_content", columnDefinition = "TEXT")
    private String routeContent;
    
    @Column(name = "generated_time", nullable = false)
    private LocalDateTime generatedTime;
    
    @Column(name = "interest_tags")
    private String interestTags; // 兴趣标签，逗号分隔
    
    @Column(name = "route_title")
    private String routeTitle; // 路线名称
    
    @Column(name = "route_tag")
    private String routeTag; // 特色标签
    
    @Column(name = "attractions_count")
    private Integer attractionsCount; // 景点数量
    
    @Column(name = "restaurants_count")
    private Integer restaurantsCount; // 餐厅数量
    
    @Column(name = "transport_mode")
    private String transportMode; // 交通方式
    
    @Column(name = "total_budget")
    private Integer totalBudget; // 总预算
    
    @Column(name = "recommendation_score")
    private Integer recommendationScore; // 推荐指数
    
    @Column(name = "daily_itinerary", columnDefinition = "TEXT")
    private String dailyItinerary; // 每日行程JSON
    
    @Column(name = "cover_image_url", columnDefinition = "TEXT")
    private String coverImageUrl; // 封面图片URL（多张图片用逗号分隔）
    
    // Constructors
    public AiGeneratedRoute() {}
    
    public AiGeneratedRoute(Long projectId, String routeContent, LocalDateTime generatedTime, String interestTags) {
        this.projectId = projectId;
        this.routeContent = routeContent;
        this.generatedTime = generatedTime;
        this.interestTags = interestTags;
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
    
    public String getRouteContent() {
        return routeContent;
    }
    
    public void setRouteContent(String routeContent) {
        this.routeContent = routeContent;
    }
    
    public LocalDateTime getGeneratedTime() {
        return generatedTime;
    }
    
    public void setGeneratedTime(LocalDateTime generatedTime) {
        this.generatedTime = generatedTime;
    }
    
    public String getInterestTags() {
        return interestTags;
    }
    
    public void setInterestTags(String interestTags) {
        this.interestTags = interestTags;
    }
    
    public String getRouteTitle() {
        return routeTitle;
    }
    
    public void setRouteTitle(String routeTitle) {
        this.routeTitle = routeTitle;
    }
    
    public String getRouteTag() {
        return routeTag;
    }
    
    public void setRouteTag(String routeTag) {
        this.routeTag = routeTag;
    }
    
    public Integer getAttractionsCount() {
        return attractionsCount;
    }
    
    public void setAttractionsCount(Integer attractionsCount) {
        this.attractionsCount = attractionsCount;
    }
    
    public Integer getRestaurantsCount() {
        return restaurantsCount;
    }
    
    public void setRestaurantsCount(Integer restaurantsCount) {
        this.restaurantsCount = restaurantsCount;
    }
    
    public String getTransportMode() {
        return transportMode;
    }
    
    public void setTransportMode(String transportMode) {
        this.transportMode = transportMode;
    }
    
    public Integer getTotalBudget() {
        return totalBudget;
    }
    
    public void setTotalBudget(Integer totalBudget) {
        this.totalBudget = totalBudget;
    }
    
    public Integer getRecommendationScore() {
        return recommendationScore;
    }
    
    public void setRecommendationScore(Integer recommendationScore) {
        this.recommendationScore = recommendationScore;
    }
    
    public String getDailyItinerary() {
        return dailyItinerary;
    }
    
    public void setDailyItinerary(String dailyItinerary) {
        this.dailyItinerary = dailyItinerary;
    }
    
    public String getCoverImageUrl() {
        return coverImageUrl;
    }
    
    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }
    
    @Override
    public String toString() {
        return "AiGeneratedRoute{" +
                "id=" + id +
                ", projectId=" + projectId +
                ", routeContent='" + routeContent + '\'' +
                ", generatedTime=" + generatedTime +
                ", interestTags='" + interestTags + '\'' +
                ", routeTitle='" + routeTitle + '\'' +
                ", routeTag='" + routeTag + '\'' +
                ", attractionsCount=" + attractionsCount +
                ", restaurantsCount=" + restaurantsCount +
                ", transportMode='" + transportMode + '\'' +
                ", totalBudget=" + totalBudget +
                ", recommendationScore=" + recommendationScore +
                '}';
    }
}