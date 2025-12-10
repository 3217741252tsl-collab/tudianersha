package com.tudianersha.service;

import com.tudianersha.entity.TravelSession;
import com.tudianersha.repository.TravelSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TravelSessionService {
    
    @Autowired
    private TravelSessionRepository travelSessionRepository;
    
    public List<TravelSession> getAllTravelSessions() {
        return travelSessionRepository.findAll();
    }
    
    public Optional<TravelSession> getTravelSessionById(Long id) {
        return travelSessionRepository.findById(id);
    }
    
    public TravelSession saveTravelSession(TravelSession travelSession) {
        return travelSessionRepository.save(travelSession);
    }
    
    public void deleteTravelSession(Long id) {
        travelSessionRepository.deleteById(id);
    }
    
    public List<TravelSession> getTravelSessionsByProjectId(Long projectId) {
        return travelSessionRepository.findByProjectIdOrderByMessageTimeAsc(projectId);
    }
    
    public List<TravelSession> getTravelSessionsByProjectIdAndUserId(Long projectId, Long userId) {
        return travelSessionRepository.findByProjectIdAndUserId(projectId, userId);
    }
}