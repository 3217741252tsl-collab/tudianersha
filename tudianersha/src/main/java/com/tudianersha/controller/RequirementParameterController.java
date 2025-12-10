package com.tudianersha.controller;

import com.tudianersha.entity.RequirementParameter;
import com.tudianersha.service.RequirementParameterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/requirement-parameters")
public class RequirementParameterController {
    
    @Autowired
    private RequirementParameterService requirementParameterService;
    
    @GetMapping
    public ResponseEntity<List<RequirementParameter>> getAllRequirementParameters() {
        List<RequirementParameter> requirementParameters = requirementParameterService.getAllRequirementParameters();
        return new ResponseEntity<>(requirementParameters, HttpStatus.OK);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<RequirementParameter> getRequirementParameterById(@PathVariable Long id) {
        Optional<RequirementParameter> requirementParameter = requirementParameterService.getRequirementParameterById(id);
        if (requirementParameter.isPresent()) {
            return new ResponseEntity<>(requirementParameter.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @PostMapping
    public ResponseEntity<RequirementParameter> createRequirementParameter(@RequestBody RequirementParameter requirementParameter) {
        RequirementParameter savedRequirementParameter = requirementParameterService.saveRequirementParameter(requirementParameter);
        return new ResponseEntity<>(savedRequirementParameter, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<RequirementParameter> updateRequirementParameter(@PathVariable Long id, @RequestBody RequirementParameter requirementParameterDetails) {
        Optional<RequirementParameter> requirementParameter = requirementParameterService.getRequirementParameterById(id);
        if (requirementParameter.isPresent()) {
            RequirementParameter existingRequirementParameter = requirementParameter.get();
            existingRequirementParameter.setProjectId(requirementParameterDetails.getProjectId());
            existingRequirementParameter.setInterestTags(requirementParameterDetails.getInterestTags());
            existingRequirementParameter.setDailyBudgetAllocation(requirementParameterDetails.getDailyBudgetAllocation());
            existingRequirementParameter.setWishlist(requirementParameterDetails.getWishlist());
            
            RequirementParameter updatedRequirementParameter = requirementParameterService.saveRequirementParameter(existingRequirementParameter);
            return new ResponseEntity<>(updatedRequirementParameter, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteRequirementParameter(@PathVariable Long id) {
        Optional<RequirementParameter> requirementParameter = requirementParameterService.getRequirementParameterById(id);
        if (requirementParameter.isPresent()) {
            requirementParameterService.deleteRequirementParameter(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping("/project/{projectId}")
    public ResponseEntity<RequirementParameter> getRequirementParameterByProjectId(@PathVariable Long projectId) {
        Optional<RequirementParameter> requirementParameter = requirementParameterService.getRequirementParameterByProjectId(projectId);
        if (requirementParameter.isPresent()) {
            return new ResponseEntity<>(requirementParameter.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping("/project/{projectId}/all")
    public ResponseEntity<List<RequirementParameter>> getAllRequirementParametersByProjectId(@PathVariable Long projectId) {
        List<RequirementParameter> requirementParameters = requirementParameterService.getAllRequirementParametersByProjectId(projectId);
        return new ResponseEntity<>(requirementParameters, HttpStatus.OK);
    }
}