package com.tudianersha.entity;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "requirement_parameters")
public class RequirementParameter {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "project_id", nullable = false)
    private Long projectId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "interest_tags")
    private String interestTags; // 兴趣标签，逗号分隔
    
    @Column(name = "daily_budget_allocation", precision = 10, scale = 2)
    private BigDecimal dailyBudgetAllocation;
    
    @Column(name = "wishlist", columnDefinition = "TEXT")
    private String wishlist; // 必去景点心愿单
    
    @Column(name = "dislike_list", columnDefinition = "TEXT")
    private String dislikeList; // 不喜欢的景点
    
    @Column(name = "budget_breakdown", columnDefinition = "TEXT")
    private String budgetBreakdown; // 预算明细
    
    // Constructors
    public RequirementParameter() {}
    
    public RequirementParameter(Long projectId, String interestTags, BigDecimal dailyBudgetAllocation, String wishlist) {
        this.projectId = projectId;
        this.interestTags = interestTags;
        this.dailyBudgetAllocation = dailyBudgetAllocation;
        this.wishlist = wishlist;
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
    
    public String getInterestTags() {
        return interestTags;
    }
    
    public void setInterestTags(String interestTags) {
        this.interestTags = interestTags;
    }
    
    public BigDecimal getDailyBudgetAllocation() {
        return dailyBudgetAllocation;
    }
    
    public void setDailyBudgetAllocation(BigDecimal dailyBudgetAllocation) {
        this.dailyBudgetAllocation = dailyBudgetAllocation;
    }
    
    public String getWishlist() {
        return wishlist;
    }
    
    public void setWishlist(String wishlist) {
        this.wishlist = wishlist;
    }
    
    public String getDislikeList() {
        return dislikeList;
    }
    
    public void setDislikeList(String dislikeList) {
        this.dislikeList = dislikeList;
    }
    
    public String getBudgetBreakdown() {
        return budgetBreakdown;
    }
    
    public void setBudgetBreakdown(String budgetBreakdown) {
        this.budgetBreakdown = budgetBreakdown;
    }
    
    @Override
    public String toString() {
        return "RequirementParameter{" +
                "id=" + id +
                ", projectId=" + projectId +
                ", userId=" + userId +
                ", interestTags='" + interestTags + '\'' +
                ", dailyBudgetAllocation=" + dailyBudgetAllocation +
                ", wishlist='" + wishlist + '\'' +
                ", dislikeList='" + dislikeList + '\'' +
                ", budgetBreakdown='" + budgetBreakdown + '\'' +
                '}';
    }
}