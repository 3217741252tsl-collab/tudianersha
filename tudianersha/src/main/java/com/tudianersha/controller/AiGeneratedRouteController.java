package com.tudianersha.controller;

import com.tudianersha.entity.AiGeneratedRoute;
import com.tudianersha.service.AiGeneratedRouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/ai-generated-routes")
public class AiGeneratedRouteController {
    
    @Autowired
    private AiGeneratedRouteService aiGeneratedRouteService;
    
    @GetMapping
    public ResponseEntity<List<AiGeneratedRoute>> getAllAiGeneratedRoutes() {
        List<AiGeneratedRoute> aiGeneratedRoutes = aiGeneratedRouteService.getAllAiGeneratedRoutes();
        return new ResponseEntity<>(aiGeneratedRoutes, HttpStatus.OK);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<AiGeneratedRoute> getAiGeneratedRouteById(@PathVariable Long id) {
        Optional<AiGeneratedRoute> aiGeneratedRoute = aiGeneratedRouteService.getAiGeneratedRouteById(id);
        if (aiGeneratedRoute.isPresent()) {
            return new ResponseEntity<>(aiGeneratedRoute.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @PostMapping
    public ResponseEntity<AiGeneratedRoute> createAiGeneratedRoute(@RequestBody AiGeneratedRoute aiGeneratedRoute) {
        AiGeneratedRoute savedAiGeneratedRoute = aiGeneratedRouteService.saveAiGeneratedRoute(aiGeneratedRoute);
        return new ResponseEntity<>(savedAiGeneratedRoute, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<AiGeneratedRoute> updateAiGeneratedRoute(@PathVariable Long id, @RequestBody AiGeneratedRoute aiGeneratedRouteDetails) {
        Optional<AiGeneratedRoute> aiGeneratedRoute = aiGeneratedRouteService.getAiGeneratedRouteById(id);
        if (aiGeneratedRoute.isPresent()) {
            AiGeneratedRoute existingAiGeneratedRoute = aiGeneratedRoute.get();
            existingAiGeneratedRoute.setProjectId(aiGeneratedRouteDetails.getProjectId());
            existingAiGeneratedRoute.setRouteContent(aiGeneratedRouteDetails.getRouteContent());
            existingAiGeneratedRoute.setGeneratedTime(aiGeneratedRouteDetails.getGeneratedTime());
            existingAiGeneratedRoute.setInterestTags(aiGeneratedRouteDetails.getInterestTags());
            
            AiGeneratedRoute updatedAiGeneratedRoute = aiGeneratedRouteService.saveAiGeneratedRoute(existingAiGeneratedRoute);
            return new ResponseEntity<>(updatedAiGeneratedRoute, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteAiGeneratedRoute(@PathVariable Long id) {
        Optional<AiGeneratedRoute> aiGeneratedRoute = aiGeneratedRouteService.getAiGeneratedRouteById(id);
        if (aiGeneratedRoute.isPresent()) {
            aiGeneratedRouteService.deleteAiGeneratedRoute(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<AiGeneratedRoute>> getAiGeneratedRoutesByProjectId(@PathVariable Long projectId) {
        List<AiGeneratedRoute> aiGeneratedRoutes = aiGeneratedRouteService.getAiGeneratedRoutesByProjectId(projectId);
        return new ResponseEntity<>(aiGeneratedRoutes, HttpStatus.OK);
    }
    
    /**
     * Generate AI routes for a project
     * POST /api/ai-generated-routes/generate/{projectId}
     */
    @PostMapping("/generate/{projectId}")
    public ResponseEntity<Map<String, Object>> generateRoutes(@PathVariable Long projectId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Delete old AI routes for this project
            List<AiGeneratedRoute> oldRoutes = aiGeneratedRouteService.getAiGeneratedRoutesByProjectId(projectId);
            for (AiGeneratedRoute route : oldRoutes) {
                aiGeneratedRouteService.deleteAiGeneratedRoute(route.getId());
            }
            
            // Generate new routes
            List<AiGeneratedRoute> routes = aiGeneratedRouteService.generateRoutesForProject(projectId);
            
            response.put("success", true);
            response.put("message", "AI路线生成成功");
            response.put("data", routes);
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "AI路线生成失败: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}