package com.tudianersha.service;

import com.tudianersha.entity.TravelParticipant;
import com.tudianersha.repository.TravelParticipantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TravelParticipantService {
    
    @Autowired
    private TravelParticipantRepository travelParticipantRepository;
    
    public List<TravelParticipant> getAllTravelParticipants() {
        return travelParticipantRepository.findAll();
    }
    
    public Optional<TravelParticipant> getTravelParticipantById(Long id) {
        return travelParticipantRepository.findById(id);
    }
    
    public TravelParticipant saveTravelParticipant(TravelParticipant travelParticipant) {
        return travelParticipantRepository.save(travelParticipant);
    }
    
    public void deleteTravelParticipant(Long id) {
        travelParticipantRepository.deleteById(id);
    }
    
    public List<TravelParticipant> getTravelParticipantsByProjectId(Long projectId) {
        return travelParticipantRepository.findByProjectId(projectId);
    }
    
    public List<TravelParticipant> getTravelParticipantsByUserId(Long userId) {
        return travelParticipantRepository.findByUserId(userId);
    }
    
    public Optional<TravelParticipant> getTravelParticipantByProjectIdAndUserId(Long projectId, Long userId) {
        return travelParticipantRepository.findByProjectIdAndUserId(projectId, userId);
    }
    
    public boolean existsByProjectIdAndUserId(Long projectId, Long userId) {
        return travelParticipantRepository.existsByProjectIdAndUserId(projectId, userId);
    }
    
    public boolean existsByProjectIdAndPermission(Long projectId, String permission) {
        return travelParticipantRepository.existsByProjectIdAndPermission(projectId, permission);
    }
}