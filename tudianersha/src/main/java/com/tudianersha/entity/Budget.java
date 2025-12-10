package com.tudianersha.entity;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "budgets")
public class Budget {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "project_id", nullable = false)
    private Long projectId;
    
    @Column(name = "total_budget", nullable = false)
    private BigDecimal totalBudget;
    
    @Column(name = "used_budget", nullable = false)
    private BigDecimal usedBudget;
    
    @Column(name = "remaining_budget", nullable = false)
    private BigDecimal remainingBudget;
    
    // Constructors
    public Budget() {}
    
    public Budget(Long projectId, BigDecimal totalBudget, BigDecimal usedBudget, BigDecimal remainingBudget) {
        this.projectId = projectId;
        this.totalBudget = totalBudget;
        this.usedBudget = usedBudget;
        this.remainingBudget = remainingBudget;
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
    
    public BigDecimal getTotalBudget() {
        return totalBudget;
    }
    
    public void setTotalBudget(BigDecimal totalBudget) {
        this.totalBudget = totalBudget;
    }
    
    public BigDecimal getUsedBudget() {
        return usedBudget;
    }
    
    public void setUsedBudget(BigDecimal usedBudget) {
        this.usedBudget = usedBudget;
    }
    
    public BigDecimal getRemainingBudget() {
        return remainingBudget;
    }
    
    public void setRemainingBudget(BigDecimal remainingBudget) {
        this.remainingBudget = remainingBudget;
    }
    
    @Override
    public String toString() {
        return "Budget{" +
                "id=" + id +
                ", projectId=" + projectId +
                ", totalBudget=" + totalBudget +
                ", usedBudget=" + usedBudget +
                ", remainingBudget=" + remainingBudget +
                '}';
    }
}