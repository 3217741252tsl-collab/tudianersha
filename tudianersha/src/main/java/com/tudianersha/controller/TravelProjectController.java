package com.tudianersha.controller;

import com.tudianersha.entity.TravelProject;
import com.tudianersha.service.TravelProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/travel-projects")
public class TravelProjectController {
    
    @Autowired
    private TravelProjectService travelProjectService;
    
    @GetMapping
    public ResponseEntity<List<TravelProject>> getAllTravelProjects() {
        List<TravelProject> travelProjects = travelProjectService.getAllTravelProjects();
        return new ResponseEntity<>(travelProjects, HttpStatus.OK);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TravelProject> getTravelProjectById(@PathVariable Long id) {
        Optional<TravelProject> travelProject = travelProjectService.getTravelProjectById(id);
        if (travelProject.isPresent()) {
            return new ResponseEntity<>(travelProject.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @PostMapping
    public ResponseEntity<TravelProject> createTravelProject(@RequestBody TravelProject travelProject) {
        TravelProject savedTravelProject = travelProjectService.saveTravelProject(travelProject);
        return new ResponseEntity<>(savedTravelProject, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TravelProject> updateTravelProject(@PathVariable Long id, @RequestBody TravelProject travelProjectDetails) {
        Optional<TravelProject> travelProject = travelProjectService.getTravelProjectById(id);
        if (travelProject.isPresent()) {
            TravelProject existingTravelProject = travelProject.get();
            
            // 只更新提供的字段（支持部分更新）
            if (travelProjectDetails.getProjectName() != null) {
                existingTravelProject.setProjectName(travelProjectDetails.getProjectName());
            }
            if (travelProjectDetails.getDestination() != null) {
                existingTravelProject.setDestination(travelProjectDetails.getDestination());
            }
            if (travelProjectDetails.getDays() != null) {
                existingTravelProject.setDays(travelProjectDetails.getDays());
            }
            if (travelProjectDetails.getCreatorId() != null) {
                existingTravelProject.setCreatorId(travelProjectDetails.getCreatorId());
            }
            if (travelProjectDetails.getStatus() != null) {
                existingTravelProject.setStatus(travelProjectDetails.getStatus());
            }
            if (travelProjectDetails.getCreatedTime() != null) {
                existingTravelProject.setCreatedTime(travelProjectDetails.getCreatedTime());
            }
            if (travelProjectDetails.getUpdatedTime() != null) {
                existingTravelProject.setUpdatedTime(travelProjectDetails.getUpdatedTime());
            }
            if (travelProjectDetails.getCurrentRouteId() != null) {
                existingTravelProject.setCurrentRouteId(travelProjectDetails.getCurrentRouteId());
            }
            
            TravelProject updatedTravelProject = travelProjectService.saveTravelProject(existingTravelProject);
            return new ResponseEntity<>(updatedTravelProject, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteTravelProject(@PathVariable Long id) {
        Optional<TravelProject> travelProject = travelProjectService.getTravelProjectById(id);
        if (travelProject.isPresent()) {
            travelProjectService.deleteTravelProject(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping("/creator/{creatorId}")
    public ResponseEntity<List<TravelProject>> getTravelProjectsByCreatorId(@PathVariable Long creatorId) {
        List<TravelProject> travelProjects = travelProjectService.getTravelProjectsByCreatorId(creatorId);
        return new ResponseEntity<>(travelProjects, HttpStatus.OK);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TravelProject>> getTravelProjectsByStatus(@PathVariable String status) {
        List<TravelProject> travelProjects = travelProjectService.getTravelProjectsByStatus(status);
        return new ResponseEntity<>(travelProjects, HttpStatus.OK);
    }
}