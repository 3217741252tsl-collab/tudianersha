package com.tudianersha.controller;

import com.tudianersha.entity.TravelParticipant;
import com.tudianersha.service.TravelParticipantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/travel-participants")
public class TravelParticipantController {
    
    @Autowired
    private TravelParticipantService travelParticipantService;
    
    @GetMapping
    public ResponseEntity<List<TravelParticipant>> getAllTravelParticipants() {
        List<TravelParticipant> travelParticipants = travelParticipantService.getAllTravelParticipants();
        return new ResponseEntity<>(travelParticipants, HttpStatus.OK);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TravelParticipant> getTravelParticipantById(@PathVariable Long id) {
        Optional<TravelParticipant> travelParticipant = travelParticipantService.getTravelParticipantById(id);
        if (travelParticipant.isPresent()) {
            return new ResponseEntity<>(travelParticipant.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @PostMapping
    public ResponseEntity<TravelParticipant> createTravelParticipant(@RequestBody TravelParticipant travelParticipant) {
        TravelParticipant savedTravelParticipant = travelParticipantService.saveTravelParticipant(travelParticipant);
        return new ResponseEntity<>(savedTravelParticipant, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TravelParticipant> updateTravelParticipant(@PathVariable Long id, @RequestBody TravelParticipant travelParticipantDetails) {
        Optional<TravelParticipant> travelParticipant = travelParticipantService.getTravelParticipantById(id);
        if (travelParticipant.isPresent()) {
            TravelParticipant existingTravelParticipant = travelParticipant.get();
            existingTravelParticipant.setProjectId(travelParticipantDetails.getProjectId());
            existingTravelParticipant.setUserId(travelParticipantDetails.getUserId());
            existingTravelParticipant.setPermission(travelParticipantDetails.getPermission());
            
            TravelParticipant updatedTravelParticipant = travelParticipantService.saveTravelParticipant(existingTravelParticipant);
            return new ResponseEntity<>(updatedTravelParticipant, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteTravelParticipant(@PathVariable Long id) {
        Optional<TravelParticipant> travelParticipant = travelParticipantService.getTravelParticipantById(id);
        if (travelParticipant.isPresent()) {
            travelParticipantService.deleteTravelParticipant(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<TravelParticipant>> getTravelParticipantsByProjectId(@PathVariable Long projectId) {
        List<TravelParticipant> travelParticipants = travelParticipantService.getTravelParticipantsByProjectId(projectId);
        return new ResponseEntity<>(travelParticipants, HttpStatus.OK);
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TravelParticipant>> getTravelParticipantsByUserId(@PathVariable Long userId) {
        List<TravelParticipant> travelParticipants = travelParticipantService.getTravelParticipantsByUserId(userId);
        return new ResponseEntity<>(travelParticipants, HttpStatus.OK);
    }
}