package com.tudianersha.service;

import com.tudianersha.entity.ActivitySchedule;
import com.tudianersha.repository.ActivityScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ActivityScheduleService {
    
    @Autowired
    private ActivityScheduleRepository activityScheduleRepository;
    
    public List<ActivitySchedule> getAllActivitySchedules() {
        return activityScheduleRepository.findAll();
    }
    
    public Optional<ActivitySchedule> getActivityScheduleById(Long id) {
        return activityScheduleRepository.findById(id);
    }
    
    public ActivitySchedule saveActivitySchedule(ActivitySchedule activitySchedule) {
        return activityScheduleRepository.save(activitySchedule);
    }
    
    public void deleteActivitySchedule(Long id) {
        activityScheduleRepository.deleteById(id);
    }
    
    public List<ActivitySchedule> getActivitySchedulesByProjectId(Long projectId) {
        return activityScheduleRepository.findByProjectId(projectId);
    }
    
    public List<ActivitySchedule> getActivitySchedulesByProjectIdOrdered(Long projectId) {
        return activityScheduleRepository.findByProjectIdOrderByDayNumberAscActivityTimeAsc(projectId);
    }
}