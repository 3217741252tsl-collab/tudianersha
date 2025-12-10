package com.tudianersha.controller;

import com.tudianersha.entity.TravelSession;
import com.tudianersha.service.TravelSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/travel-sessions")
public class TravelSessionController {
    
    @Autowired
    private TravelSessionService travelSessionService;
    
    @GetMapping
    public ResponseEntity<List<TravelSession>> getAllTravelSessions() {
        List<TravelSession> travelSessions = travelSessionService.getAllTravelSessions();
        return new ResponseEntity<>(travelSessions, HttpStatus.OK);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TravelSession> getTravelSessionById(@PathVariable Long id) {
        Optional<TravelSession> travelSession = travelSessionService.getTravelSessionById(id);
        if (travelSession.isPresent()) {
            return new ResponseEntity<>(travelSession.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @PostMapping
    public ResponseEntity<TravelSession> createTravelSession(@RequestBody TravelSession travelSession) {
        TravelSession savedTravelSession = travelSessionService.saveTravelSession(travelSession);
        return new ResponseEntity<>(savedTravelSession, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TravelSession> updateTravelSession(@PathVariable Long id, @RequestBody TravelSession travelSessionDetails) {
        Optional<TravelSession> travelSession = travelSessionService.getTravelSessionById(id);
        if (travelSession.isPresent()) {
            TravelSession existingTravelSession = travelSession.get();
            existingTravelSession.setProjectId(travelSessionDetails.getProjectId());
            existingTravelSession.setUserId(travelSessionDetails.getUserId());
            existingTravelSession.setMessage(travelSessionDetails.getMessage());
            existingTravelSession.setMessageTime(travelSessionDetails.getMessageTime());
            existingTravelSession.setMentionedUserId(travelSessionDetails.getMentionedUserId());
            
            TravelSession updatedTravelSession = travelSessionService.saveTravelSession(existingTravelSession);
            return new ResponseEntity<>(updatedTravelSession, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteTravelSession(@PathVariable Long id) {
        Optional<TravelSession> travelSession = travelSessionService.getTravelSessionById(id);
        if (travelSession.isPresent()) {
            travelSessionService.deleteTravelSession(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<TravelSession>> getTravelSessionsByProjectId(@PathVariable Long projectId) {
        List<TravelSession> travelSessions = travelSessionService.getTravelSessionsByProjectId(projectId);
        return new ResponseEntity<>(travelSessions, HttpStatus.OK);
    }
    
    @GetMapping("/project/{projectId}/user/{userId}")
    public ResponseEntity<List<TravelSession>> getTravelSessionsByProjectIdAndUserId(@PathVariable Long projectId, @PathVariable Long userId) {
        List<TravelSession> travelSessions = travelSessionService.getTravelSessionsByProjectIdAndUserId(projectId, userId);
        return new ResponseEntity<>(travelSessions, HttpStatus.OK);
    }
}