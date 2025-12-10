package com.tudianersha.service;

import com.tudianersha.entity.TravelProject;
import com.tudianersha.repository.TravelProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TravelProjectService {
    
    @Autowired
    private TravelProjectRepository travelProjectRepository;
    
    public List<TravelProject> getAllTravelProjects() {
        return travelProjectRepository.findAll();
    }
    
    public Optional<TravelProject> getTravelProjectById(Long id) {
        return travelProjectRepository.findById(id);
    }
    
    public TravelProject saveTravelProject(TravelProject travelProject) {
        return travelProjectRepository.save(travelProject);
    }
    
    public void deleteTravelProject(Long id) {
        travelProjectRepository.deleteById(id);
    }
    
    public List<TravelProject> getTravelProjectsByCreatorId(Long creatorId) {
        return travelProjectRepository.findByCreatorId(creatorId);
    }
    
    public List<TravelProject> getTravelProjectsByStatus(String status) {
        return travelProjectRepository.findByStatus(status);
    }
}