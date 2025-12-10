package com.tudianersha.controller;

import com.tudianersha.entity.Budget;
import com.tudianersha.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {
    
    @Autowired
    private BudgetService budgetService;
    
    @GetMapping
    public ResponseEntity<List<Budget>> getAllBudgets() {
        List<Budget> budgets = budgetService.getAllBudgets();
        return new ResponseEntity<>(budgets, HttpStatus.OK);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Budget> getBudgetById(@PathVariable Long id) {
        Optional<Budget> budget = budgetService.getBudgetById(id);
        if (budget.isPresent()) {
            return new ResponseEntity<>(budget.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @PostMapping
    public ResponseEntity<Budget> createBudget(@RequestBody Budget budget) {
        Budget savedBudget = budgetService.saveBudget(budget);
        return new ResponseEntity<>(savedBudget, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Budget> updateBudget(@PathVariable Long id, @RequestBody Budget budgetDetails) {
        Optional<Budget> budget = budgetService.getBudgetById(id);
        if (budget.isPresent()) {
            Budget existingBudget = budget.get();
            existingBudget.setProjectId(budgetDetails.getProjectId());
            existingBudget.setTotalBudget(budgetDetails.getTotalBudget());
            existingBudget.setUsedBudget(budgetDetails.getUsedBudget());
            existingBudget.setRemainingBudget(budgetDetails.getRemainingBudget());
            
            Budget updatedBudget = budgetService.saveBudget(existingBudget);
            return new ResponseEntity<>(updatedBudget, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteBudget(@PathVariable Long id) {
        Optional<Budget> budget = budgetService.getBudgetById(id);
        if (budget.isPresent()) {
            budgetService.deleteBudget(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping("/project/{projectId}")
    public ResponseEntity<Budget> getBudgetByProjectId(@PathVariable Long projectId) {
        Optional<Budget> budget = budgetService.getBudgetByProjectId(projectId);
        if (budget.isPresent()) {
            return new ResponseEntity<>(budget.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}