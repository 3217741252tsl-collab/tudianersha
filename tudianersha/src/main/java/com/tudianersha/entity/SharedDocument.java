package com.tudianersha.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "shared_documents")
public class SharedDocument {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "project_id", nullable = false)
    private Long projectId;
    
    @Column(name = "document_url")
    private String documentUrl;
    
    @Column(name = "format")
    private String format; // PDF等格式
    
    @Column(name = "generated_time", nullable = false)
    private LocalDateTime generatedTime;
    
    @Column(name = "share_link")
    private String shareLink;
    
    @Column(name = "creator_id", nullable = false)
    private Long creatorId;
    
    // Constructors
    public SharedDocument() {}
    
    public SharedDocument(Long projectId, String documentUrl, String format, LocalDateTime generatedTime, 
                         String shareLink, Long creatorId) {
        this.projectId = projectId;
        this.documentUrl = documentUrl;
        this.format = format;
        this.generatedTime = generatedTime;
        this.shareLink = shareLink;
        this.creatorId = creatorId;
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
    
    public String getDocumentUrl() {
        return documentUrl;
    }
    
    public void setDocumentUrl(String documentUrl) {
        this.documentUrl = documentUrl;
    }
    
    public String getFormat() {
        return format;
    }
    
    public void setFormat(String format) {
        this.format = format;
    }
    
    public LocalDateTime getGeneratedTime() {
        return generatedTime;
    }
    
    public void setGeneratedTime(LocalDateTime generatedTime) {
        this.generatedTime = generatedTime;
    }
    
    public String getShareLink() {
        return shareLink;
    }
    
    public void setShareLink(String shareLink) {
        this.shareLink = shareLink;
    }
    
    public Long getCreatorId() {
        return creatorId;
    }
    
    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }
    
    @Override
    public String toString() {
        return "SharedDocument{" +
                "id=" + id +
                ", projectId=" + projectId +
                ", documentUrl='" + documentUrl + '\'' +
                ", format='" + format + '\'' +
                ", generatedTime=" + generatedTime +
                ", shareLink='" + shareLink + '\'' +
                ", creatorId=" + creatorId +
                '}';
    }
}