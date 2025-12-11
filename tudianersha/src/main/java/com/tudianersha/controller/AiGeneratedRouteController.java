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
        System.out.println("[UPDATE] Updating route ID: " + id);
        System.out.println("[UPDATE] DailyItinerary received: " + (aiGeneratedRouteDetails.getDailyItinerary() != null ? aiGeneratedRouteDetails.getDailyItinerary().substring(0, Math.min(100, aiGeneratedRouteDetails.getDailyItinerary().length())) + "..." : "null"));
        
        Optional<AiGeneratedRoute> aiGeneratedRoute = aiGeneratedRouteService.getAiGeneratedRouteById(id);
        if (aiGeneratedRoute.isPresent()) {
            AiGeneratedRoute existingAiGeneratedRoute = aiGeneratedRoute.get();
            
            // 更新提供的字段
            if (aiGeneratedRouteDetails.getProjectId() != null) {
                existingAiGeneratedRoute.setProjectId(aiGeneratedRouteDetails.getProjectId());
            }
            if (aiGeneratedRouteDetails.getRouteContent() != null) {
                existingAiGeneratedRoute.setRouteContent(aiGeneratedRouteDetails.getRouteContent());
            }
            if (aiGeneratedRouteDetails.getGeneratedTime() != null) {
                existingAiGeneratedRoute.setGeneratedTime(aiGeneratedRouteDetails.getGeneratedTime());
            }
            if (aiGeneratedRouteDetails.getInterestTags() != null) {
                existingAiGeneratedRoute.setInterestTags(aiGeneratedRouteDetails.getInterestTags());
            }
            // 重要：更新 dailyItinerary 字段
            if (aiGeneratedRouteDetails.getDailyItinerary() != null) {
                existingAiGeneratedRoute.setDailyItinerary(aiGeneratedRouteDetails.getDailyItinerary());
                System.out.println("[UPDATE] DailyItinerary updated!");
            } else {
                System.out.println("[UPDATE] Warning: DailyItinerary is null, not updating!");
            }
            // 同时更新其他字段
            if (aiGeneratedRouteDetails.getRouteTitle() != null) {
                existingAiGeneratedRoute.setRouteTitle(aiGeneratedRouteDetails.getRouteTitle());
            }
            if (aiGeneratedRouteDetails.getRouteTag() != null) {
                existingAiGeneratedRoute.setRouteTag(aiGeneratedRouteDetails.getRouteTag());
            }
            if (aiGeneratedRouteDetails.getAttractionsCount() != null) {
                existingAiGeneratedRoute.setAttractionsCount(aiGeneratedRouteDetails.getAttractionsCount());
            }
            if (aiGeneratedRouteDetails.getRestaurantsCount() != null) {
                existingAiGeneratedRoute.setRestaurantsCount(aiGeneratedRouteDetails.getRestaurantsCount());
            }
            if (aiGeneratedRouteDetails.getTransportMode() != null) {
                existingAiGeneratedRoute.setTransportMode(aiGeneratedRouteDetails.getTransportMode());
            }
            if (aiGeneratedRouteDetails.getTotalBudget() != null) {
                existingAiGeneratedRoute.setTotalBudget(aiGeneratedRouteDetails.getTotalBudget());
            }
            if (aiGeneratedRouteDetails.getRecommendationScore() != null) {
                existingAiGeneratedRoute.setRecommendationScore(aiGeneratedRouteDetails.getRecommendationScore());
            }
            if (aiGeneratedRouteDetails.getCoverImageUrl() != null) {
                existingAiGeneratedRoute.setCoverImageUrl(aiGeneratedRouteDetails.getCoverImageUrl());
            }
            
            AiGeneratedRoute updatedAiGeneratedRoute = aiGeneratedRouteService.saveAiGeneratedRoute(existingAiGeneratedRoute);
            System.out.println("[UPDATE] Route saved successfully!");
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
    
    /**
     * Save activity budgets for a route
     * PUT /api/ai-generated-routes/{id}/budgets
     */
    @PutMapping("/{id}/budgets")
    public ResponseEntity<Map<String, Object>> saveBudgets(@PathVariable Long id, @RequestBody Map<String, Double> budgets) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<AiGeneratedRoute> aiGeneratedRoute = aiGeneratedRouteService.getAiGeneratedRouteById(id);
            if (aiGeneratedRoute.isPresent()) {
                AiGeneratedRoute route = aiGeneratedRoute.get();
                // 将budgets存储为JSON字符串
                String budgetsJson = new com.google.gson.Gson().toJson(budgets);
                route.setBudgetsJson(budgetsJson);
                aiGeneratedRouteService.saveAiGeneratedRoute(route);
                
                response.put("success", true);
                response.put("message", "预算保存成功");
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response.put("success", false);
                response.put("message", "路线不存在");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "保存失败: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get activity budgets for a route
     * GET /api/ai-generated-routes/{id}/budgets
     */
    @GetMapping("/{id}/budgets")
    public ResponseEntity<Map<String, Double>> getBudgets(@PathVariable Long id) {
        try {
            Optional<AiGeneratedRoute> aiGeneratedRoute = aiGeneratedRouteService.getAiGeneratedRouteById(id);
            if (aiGeneratedRoute.isPresent()) {
                AiGeneratedRoute route = aiGeneratedRoute.get();
                String budgetsJson = route.getBudgetsJson();
                
                if (budgetsJson != null && !budgetsJson.isEmpty()) {
                    // 从JSON字符串解析budgets
                    @SuppressWarnings("unchecked")
                    Map<String, Double> budgets = new com.google.gson.Gson().fromJson(
                        budgetsJson, 
                        new com.google.gson.reflect.TypeToken<Map<String, Double>>(){}.getType()
                    );
                    return new ResponseEntity<>(budgets, HttpStatus.OK);
                } else {
                    // 返回空对象
                    return new ResponseEntity<>(new HashMap<>(), HttpStatus.OK);
                }
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(new HashMap<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}