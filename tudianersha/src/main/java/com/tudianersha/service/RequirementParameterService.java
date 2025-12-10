package com.tudianersha.service;

import com.tudianersha.entity.RequirementParameter;
import com.tudianersha.repository.RequirementParameterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RequirementParameterService {
    
    @Autowired
    private RequirementParameterRepository requirementParameterRepository;
    
    public List<RequirementParameter> getAllRequirementParameters() {
        return requirementParameterRepository.findAll();
    }
    
    public Optional<RequirementParameter> getRequirementParameterById(Long id) {
        return requirementParameterRepository.findById(id);
    }
    
    public RequirementParameter saveRequirementParameter(RequirementParameter requirementParameter) {
        return requirementParameterRepository.save(requirementParameter);
    }
    
    public void deleteRequirementParameter(Long id) {
        requirementParameterRepository.deleteById(id);
    }
    
    public Optional<RequirementParameter> getRequirementParameterByProjectId(Long projectId) {
        return requirementParameterRepository.findByProjectId(projectId);
    }
    
    public List<RequirementParameter> getAllRequirementParametersByProjectId(Long projectId) {
        return requirementParameterRepository.findAllByProjectId(projectId);
    }
}