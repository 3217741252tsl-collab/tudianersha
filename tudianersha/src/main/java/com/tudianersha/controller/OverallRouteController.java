package com.tudianersha.controller;

import com.tudianersha.entity.OverallRoute;
import com.tudianersha.service.OverallRouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/overall-routes")
public class OverallRouteController {
    
    @Autowired
    private OverallRouteService overallRouteService;
    
    @GetMapping
    public ResponseEntity<List<OverallRoute>> getAllOverallRoutes() {
        List<OverallRoute> overallRoutes = overallRouteService.getAllOverallRoutes();
        return new ResponseEntity<>(overallRoutes, HttpStatus.OK);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<OverallRoute> getOverallRouteById(@PathVariable Long id) {
        Optional<OverallRoute> overallRoute = overallRouteService.getOverallRouteById(id);
        if (overallRoute.isPresent()) {
            return new ResponseEntity<>(overallRoute.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @PostMapping
    public ResponseEntity<OverallRoute> createOverallRoute(@RequestBody OverallRoute overallRoute) {
        OverallRoute savedOverallRoute = overallRouteService.saveOverallRoute(overallRoute);
        return new ResponseEntity<>(savedOverallRoute, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<OverallRoute> updateOverallRoute(@PathVariable Long id, @RequestBody OverallRoute overallRouteDetails) {
        Optional<OverallRoute> overallRoute = overallRouteService.getOverallRouteById(id);
        if (overallRoute.isPresent()) {
            OverallRoute existingOverallRoute = overallRoute.get();
            existingOverallRoute.setProjectId(overallRouteDetails.getProjectId());
            existingOverallRoute.setRouteDetails(overallRouteDetails.getRouteDetails());
            existingOverallRoute.setCreatedTime(overallRouteDetails.getCreatedTime());
            
            OverallRoute updatedOverallRoute = overallRouteService.saveOverallRoute(existingOverallRoute);
            return new ResponseEntity<>(updatedOverallRoute, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteOverallRoute(@PathVariable Long id) {
        Optional<OverallRoute> overallRoute = overallRouteService.getOverallRouteById(id);
        if (overallRoute.isPresent()) {
            overallRouteService.deleteOverallRoute(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<OverallRoute>> getOverallRoutesByProjectId(@PathVariable Long projectId) {
        List<OverallRoute> overallRoutes = overallRouteService.getOverallRoutesByProjectId(projectId);
        return new ResponseEntity<>(overallRoutes, HttpStatus.OK);
    }
}