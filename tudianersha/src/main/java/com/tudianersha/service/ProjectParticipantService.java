package com.tudianersha.service;

import com.tudianersha.entity.ProjectParticipant;
import com.tudianersha.repository.ProjectParticipantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectParticipantService {
    
    @Autowired
    private ProjectParticipantRepository projectParticipantRepository;
    
    public List<ProjectParticipant> getAllProjectParticipants() {
        return projectParticipantRepository.findAll();
    }
    
    public Optional<ProjectParticipant> getProjectParticipantById(Long id) {
        return projectParticipantRepository.findById(id);
    }
    
    public ProjectParticipant saveProjectParticipant(ProjectParticipant projectParticipant) {
        return projectParticipantRepository.save(projectParticipant);
    }
    
    public void deleteProjectParticipant(Long id) {
        projectParticipantRepository.deleteById(id);
    }
    
    public List<ProjectParticipant> getProjectParticipantsByProjectId(Long projectId) {
        return projectParticipantRepository.findByProjectId(projectId);
    }
    
    public List<ProjectParticipant> getProjectParticipantsByUserId(Long userId) {
        return projectParticipantRepository.findByUserId(userId);
    }
    
    public Optional<ProjectParticipant> getProjectParticipantByProjectIdAndUserId(Long projectId, Long userId) {
        return projectParticipantRepository.findByProjectIdAndUserId(projectId, userId);
    }
    
    public boolean existsByProjectIdAndUserId(Long projectId, Long userId) {
        return projectParticipantRepository.existsByProjectIdAndUserId(projectId, userId);
    }
}