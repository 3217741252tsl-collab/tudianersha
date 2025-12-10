package com.tudianersha.controller;

import com.tudianersha.entity.ProjectParticipant;
import com.tudianersha.service.ProjectParticipantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/project-participants")
public class ProjectParticipantController {
    
    @Autowired
    private ProjectParticipantService projectParticipantService;
    
    @GetMapping
    public ResponseEntity<List<ProjectParticipant>> getAllProjectParticipants() {
        List<ProjectParticipant> projectParticipants = projectParticipantService.getAllProjectParticipants();
        return new ResponseEntity<>(projectParticipants, HttpStatus.OK);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ProjectParticipant> getProjectParticipantById(@PathVariable Long id) {
        Optional<ProjectParticipant> projectParticipant = projectParticipantService.getProjectParticipantById(id);
        if (projectParticipant.isPresent()) {
            return new ResponseEntity<>(projectParticipant.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @PostMapping
    public ResponseEntity<ProjectParticipant> createProjectParticipant(@RequestBody ProjectParticipant projectParticipant) {
        ProjectParticipant savedProjectParticipant = projectParticipantService.saveProjectParticipant(projectParticipant);
        return new ResponseEntity<>(savedProjectParticipant, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ProjectParticipant> updateProjectParticipant(@PathVariable Long id, @RequestBody ProjectParticipant projectParticipantDetails) {
        Optional<ProjectParticipant> projectParticipant = projectParticipantService.getProjectParticipantById(id);
        if (projectParticipant.isPresent()) {
            ProjectParticipant existingProjectParticipant = projectParticipant.get();
            
            // 只更新提供的字段
            if (projectParticipantDetails.getProjectId() != null) {
                existingProjectParticipant.setProjectId(projectParticipantDetails.getProjectId());
            }
            if (projectParticipantDetails.getUserId() != null) {
                existingProjectParticipant.setUserId(projectParticipantDetails.getUserId());
            }
            if (projectParticipantDetails.getJoinTime() != null) {
                existingProjectParticipant.setJoinTime(projectParticipantDetails.getJoinTime());
            }
            if (projectParticipantDetails.getRole() != null) {
                existingProjectParticipant.setRole(projectParticipantDetails.getRole());
            }
            
            ProjectParticipant updatedProjectParticipant = projectParticipantService.saveProjectParticipant(existingProjectParticipant);
            return new ResponseEntity<>(updatedProjectParticipant, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteProjectParticipant(@PathVariable Long id) {
        Optional<ProjectParticipant> projectParticipant = projectParticipantService.getProjectParticipantById(id);
        if (projectParticipant.isPresent()) {
            projectParticipantService.deleteProjectParticipant(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<ProjectParticipant>> getProjectParticipantsByProjectId(@PathVariable Long projectId) {
        List<ProjectParticipant> projectParticipants = projectParticipantService.getProjectParticipantsByProjectId(projectId);
        return new ResponseEntity<>(projectParticipants, HttpStatus.OK);
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ProjectParticipant>> getProjectParticipantsByUserId(@PathVariable Long userId) {
        List<ProjectParticipant> projectParticipants = projectParticipantService.getProjectParticipantsByUserId(userId);
        return new ResponseEntity<>(projectParticipants, HttpStatus.OK);
    }
}