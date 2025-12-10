package com.tudianersha.service;

import com.tudianersha.entity.OverallRoute;
import com.tudianersha.repository.OverallRouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OverallRouteService {
    
    @Autowired
    private OverallRouteRepository overallRouteRepository;
    
    public List<OverallRoute> getAllOverallRoutes() {
        return overallRouteRepository.findAll();
    }
    
    public Optional<OverallRoute> getOverallRouteById(Long id) {
        return overallRouteRepository.findById(id);
    }
    
    public OverallRoute saveOverallRoute(OverallRoute overallRoute) {
        return overallRouteRepository.save(overallRoute);
    }
    
    public void deleteOverallRoute(Long id) {
        overallRouteRepository.deleteById(id);
    }
    
    public List<OverallRoute> getOverallRoutesByProjectId(Long projectId) {
        return overallRouteRepository.findByProjectId(projectId);
    }
}