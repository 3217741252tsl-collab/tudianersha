package com.tudianersha.controller;

import com.tudianersha.entity.ActivitySchedule;
import com.tudianersha.service.ActivityScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/activity-schedules")
public class ActivityScheduleController {
    
    @Autowired
    private ActivityScheduleService activityScheduleService;
    
    @GetMapping
    public ResponseEntity<List<ActivitySchedule>> getAllActivitySchedules() {
        List<ActivitySchedule> activitySchedules = activityScheduleService.getAllActivitySchedules();
        return new ResponseEntity<>(activitySchedules, HttpStatus.OK);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ActivitySchedule> getActivityScheduleById(@PathVariable Long id) {
        Optional<ActivitySchedule> activitySchedule = activityScheduleService.getActivityScheduleById(id);
        if (activitySchedule.isPresent()) {
            return new ResponseEntity<>(activitySchedule.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @PostMapping
    public ResponseEntity<ActivitySchedule> createActivitySchedule(@RequestBody ActivitySchedule activitySchedule) {
        ActivitySchedule savedActivitySchedule = activityScheduleService.saveActivitySchedule(activitySchedule);
        return new ResponseEntity<>(savedActivitySchedule, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ActivitySchedule> updateActivitySchedule(@PathVariable Long id, @RequestBody ActivitySchedule activityScheduleDetails) {
        Optional<ActivitySchedule> activitySchedule = activityScheduleService.getActivityScheduleById(id);
        if (activitySchedule.isPresent()) {
            ActivitySchedule existingActivitySchedule = activitySchedule.get();
            existingActivitySchedule.setProjectId(activityScheduleDetails.getProjectId());
            existingActivitySchedule.setActivityName(activityScheduleDetails.getActivityName());
            existingActivitySchedule.setActivityTime(activityScheduleDetails.getActivityTime());
            existingActivitySchedule.setLocation(activityScheduleDetails.getLocation());
            existingActivitySchedule.setBudget(activityScheduleDetails.getBudget());
            existingActivitySchedule.setDayNumber(activityScheduleDetails.getDayNumber());
            
            ActivitySchedule updatedActivitySchedule = activityScheduleService.saveActivitySchedule(existingActivitySchedule);
            return new ResponseEntity<>(updatedActivitySchedule, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteActivitySchedule(@PathVariable Long id) {
        Optional<ActivitySchedule> activitySchedule = activityScheduleService.getActivityScheduleById(id);
        if (activitySchedule.isPresent()) {
            activityScheduleService.deleteActivitySchedule(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<ActivitySchedule>> getActivitySchedulesByProjectId(@PathVariable Long projectId) {
        List<ActivitySchedule> activitySchedules = activityScheduleService.getActivitySchedulesByProjectId(projectId);
        return new ResponseEntity<>(activitySchedules, HttpStatus.OK);
    }
    
    @GetMapping("/project/{projectId}/ordered")
    public ResponseEntity<List<ActivitySchedule>> getActivitySchedulesByProjectIdOrdered(@PathVariable Long projectId) {
        List<ActivitySchedule> activitySchedules = activityScheduleService.getActivitySchedulesByProjectIdOrdered(projectId);
        return new ResponseEntity<>(activitySchedules, HttpStatus.OK);
    }
}