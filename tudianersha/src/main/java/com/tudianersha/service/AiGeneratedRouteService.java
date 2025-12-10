package com.tudianersha.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tudianersha.entity.AiGeneratedRoute;
import com.tudianersha.entity.RequirementParameter;
import com.tudianersha.entity.TravelProject;
import com.tudianersha.entity.User;
import com.tudianersha.repository.AiGeneratedRouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AiGeneratedRouteService {
    
    @Autowired
    private AiGeneratedRouteRepository aiGeneratedRouteRepository;
    
    @Autowired
    private KimiAIService kimiAIService;
    
    @Autowired
    private TravelProjectService travelProjectService;
    
    @Autowired
    private RequirementParameterService requirementParameterService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private AmapPoiService amapPoiService;
    
    public List<AiGeneratedRoute> getAllAiGeneratedRoutes() {
        return aiGeneratedRouteRepository.findAll();
    }
    
    public Optional<AiGeneratedRoute> getAiGeneratedRouteById(Long id) {
        return aiGeneratedRouteRepository.findById(id);
    }
    
    public AiGeneratedRoute saveAiGeneratedRoute(AiGeneratedRoute aiGeneratedRoute) {
        return aiGeneratedRouteRepository.save(aiGeneratedRoute);
    }
    
    public void deleteAiGeneratedRoute(Long id) {
        aiGeneratedRouteRepository.deleteById(id);
    }
    
    public List<AiGeneratedRoute> getAiGeneratedRoutesByProjectId(Long projectId) {
        return aiGeneratedRouteRepository.findByProjectId(projectId);
    }
    
    /**
     * Generate 3 AI routes for a project based on all participants' requirements
     * 
     * @param projectId The project ID
     * @return List of generated routes
     * @throws IOException if AI API call fails
     */
    public List<AiGeneratedRoute> generateRoutesForProject(Long projectId) throws IOException {
        // 1. Get project info
        Optional<TravelProject> projectOpt = travelProjectService.getTravelProjectById(projectId);
        if (!projectOpt.isPresent()) {
            throw new IllegalArgumentException("Project not found: " + projectId);
        }
        TravelProject project = projectOpt.get();
        
        // 2. Get all requirements for this project
        List<RequirementParameter> requirements = requirementParameterService.getAllRequirementParametersByProjectId(projectId);
        if (requirements.isEmpty()) {
            throw new IllegalArgumentException("No requirements found for project: " + projectId);
        }
        
        // 3. Build prompt
        String prompt = buildPrompt(project, requirements);
        
        // 4. Call AI to generate routes
        String aiResponse = kimiAIService.generateRoute(prompt);
        
        // 5. Parse and save routes
        return parseAndSaveRoutes(projectId, aiResponse);
    }
    
    /**
     * Build AI prompt from project and requirements
     */
    private String buildPrompt(TravelProject project, List<RequirementParameter> requirements) {
        StringBuilder prompt = new StringBuilder();
        
        // Project basic info
        prompt.append("请为以下旅行项目生成3条不同风格的详细路线方案:\n\n");
        prompt.append("项目名称：").append(project.getProjectName()).append("\n");
        prompt.append("目的地：").append(project.getDestination()).append("\n");
        prompt.append("出行时间：").append(project.getStartDate()).append(" 至 ").append(project.getEndDate()).append("\n");
        prompt.append("行程天数：").append(project.getDays()).append("天\n");
        prompt.append("参与人数：").append(requirements.size()).append("人\n\n");
        
        // All participants' requirements
        prompt.append("参与者需求汇总：\n");
        for (int i = 0; i < requirements.size(); i++) {
            RequirementParameter req = requirements.get(i);
            prompt.append("\n参与者").append(i + 1).append("：\n");
            
            if (req.getInterestTags() != null && !req.getInterestTags().isEmpty()) {
                prompt.append("- 兴趣标签：").append(req.getInterestTags()).append("\n");
            }
            if (req.getWishlist() != null && !req.getWishlist().isEmpty()) {
                prompt.append("- 想去的景点：").append(req.getWishlist()).append("\n");
            }
            if (req.getDislikeList() != null && !req.getDislikeList().isEmpty()) {
                prompt.append("- 不想去：").append(req.getDislikeList()).append("\n");
            }
            if (req.getDailyBudgetAllocation() != null) {
                prompt.append("- 每日预算：¥").append(req.getDailyBudgetAllocation()).append("\n");
            }
        }
        
        // Requirements for AI
        prompt.append("\n\n请生成3条不同风格的路线方案，每条路线需要包含：\n");
        prompt.append("1. 路线名称（例如：经典全景路线、美食寻味路线等）\n");
        prompt.append("2. 路线特色标签（例如：均衡体验、吃货首选等）\n");
        prompt.append("3. 每日详细行程安排（精确到小时，包含具体时间点）\n");
        prompt.append("4. 包含景点数量\n");
        prompt.append("5. 推荐餐厅数量\n");
        prompt.append("6. 交通方式说明\n");
        prompt.append("7. 总预算估算\n");
        prompt.append("8. 推荐指数（0-100）\n\n");
        
        prompt.append("【重要！必须遵守的规则】\n");
        prompt.append("1. 所有景点必须是").append(project.getDestination()).append("真实存在的著名景点\n");
        prompt.append("2. 景点名称必须准确，能在高德地图中搜索到\n");
        prompt.append("3. 每个景点活动必须包含以下信息格式：\n");
        prompt.append("   - 时间段（如09:00-12:00）\n");
        prompt.append("   - 景点名称（真实准确）\n");
        prompt.append("   - 开放时间说明（如：全天开放/8:00-18:00）\n");
        prompt.append("   - 门票信息（如：免费/60元/需预约）\n");
        prompt.append("   - 游玩时长建议\n");
        prompt.append("4. 餐厅推荐也必须是真实存在的知名餐厅\n");
        prompt.append("5. 时间安排必须合理，考虑景点开放时间\n");
        prompt.append("6. 景点之间的交通时间要真实考虑距离\n");
        prompt.append("7. 避免安排需要预约但当天无法预约的景点\n\n");
        
        prompt.append("请严格按照以下JSON格式返回3条路线：\n");
        prompt.append("```json\n");
        prompt.append("{\n");
        prompt.append("  \"routes\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"title\": \"路线名称\",\n");
        prompt.append("      \"tag\": \"特色标签\",\n");
        prompt.append("      \"attractions\": 景点数量,\n");
        prompt.append("      \"restaurants\": 餐厅数量,\n");
        prompt.append("      \"transport\": \"交通方式\",\n");
        prompt.append("      \"budget\": 总预算,\n");
        prompt.append("      \"score\": 推荐指数,\n");
        prompt.append("      \"dailyItinerary\": [\n");
        prompt.append("        {\n");
        prompt.append("          \"day\": 1,\n");
        prompt.append("          \"activities\": [\n");
        prompt.append("            \"08:00-09:00 早餐：【真实餐厅名】，特色：XX，人均：XX元\",\n");
        prompt.append("            \"09:30-12:00 景点：【真实景点名】，开放时间：8:00-18:00，门票：60元，建议游玩2.5小时\",\n");
        prompt.append("            \"12:00-13:30 午餐：【真实餐厅名】，特色：XX，人均：XX元\",\n");
        prompt.append("            \"14:00-17:00 景点：【真实景点名】，开放时间：全天，门票：免费，建议游玩3小时\",\n");
        prompt.append("            \"17:30-19:00 晚餐：【真实餐厅名】，特色：XX，人均：XX元\",\n");
        prompt.append("            \"19:30-21:00 景点：【真实景点名】（夜景），开放时间：全天，门票：免费\"\n");
        prompt.append("          ]\n");
        prompt.append("        }\n");
        prompt.append("      ]\n");
        prompt.append("    }\n");
        prompt.append("  ]\n");
        prompt.append("}\n");
        prompt.append("```\n");
        prompt.append("\n再次强调：\n");
        prompt.append("1. 每个活动必须包含具体的时间段（如：08:00-09:00）\n");
        prompt.append("2. 景点/餐厅名称要真实准确，能在高德地图搜索到\n");
        prompt.append("3. 必须标注景点的开放时间和门票信息\n");
        prompt.append("4. 合理安排早中晚三餐时间\n");
        prompt.append("5. 考虑景点之间的实际交通时间\n");
        prompt.append("6. 每天安排6-8个时间段的活动\n");
        prompt.append("7. 如果景点需要预约，请在描述中注明\n");
        
        return prompt.toString();
    }
    
    /**
     * Parse AI response and save routes to database
     */
    private List<AiGeneratedRoute> parseAndSaveRoutes(Long projectId, String aiResponse) {
        List<AiGeneratedRoute> savedRoutes = new ArrayList<>();
        Gson gson = new Gson();
        
        // 获取项目信息（用于获取目的地）
        TravelProject project = null;
        try {
            Optional<TravelProject> projectOpt = travelProjectService.getTravelProjectById(projectId);
            if (projectOpt.isPresent()) {
                project = projectOpt.get();
            }
        } catch (Exception e) {
            System.err.println("获取项目信息失败: " + e.getMessage());
        }
        
        final TravelProject finalProject = project;
        
        try {
            // Extract JSON from markdown code block if present
            String jsonContent = extractJsonFromResponse(aiResponse);
            
            System.out.println("Extracted JSON content: " + jsonContent); // Debug log
            
            // Parse JSON
            JsonObject jsonResponse = gson.fromJson(jsonContent, JsonObject.class);
            
            if (jsonResponse.has("routes")) {
                JsonArray routes = jsonResponse.getAsJsonArray("routes");
                System.out.println("解析到 " + routes.size() + " 条路线"); // Debug log
                
                for (int i = 0; i < Math.min(3, routes.size()); i++) {
                    JsonObject routeObj = routes.get(i).getAsJsonObject();
                    
                    System.out.println("路线 " + (i+1) + ": " + routeObj); // Debug log
                    
                    AiGeneratedRoute route = new AiGeneratedRoute();
                    route.setProjectId(projectId);
                    route.setGeneratedTime(LocalDateTime.now());
                    route.setRouteContent(aiResponse); // 保存完整响应
                    
                    // 提取结构化字段
                    if (routeObj.has("title")) {
                        route.setRouteTitle(routeObj.get("title").getAsString());
                    } else {
                        route.setRouteTitle("方案" + (i + 1));
                    }
                    
                    if (routeObj.has("tag")) {
                        route.setRouteTag(routeObj.get("tag").getAsString());
                    } else {
                        route.setRouteTag("AI推荐");
                    }
                    
                    if (routeObj.has("attractions")) {
                        route.setAttractionsCount(routeObj.get("attractions").getAsInt());
                    } else {
                        route.setAttractionsCount(10);
                    }
                    
                    if (routeObj.has("restaurants")) {
                        route.setRestaurantsCount(routeObj.get("restaurants").getAsInt());
                    } else {
                        route.setRestaurantsCount(6);
                    }
                    
                    if (routeObj.has("transport")) {
                        route.setTransportMode(routeObj.get("transport").getAsString());
                    } else {
                        route.setTransportMode("公共交通");
                    }
                    
                    if (routeObj.has("budget")) {
                        route.setTotalBudget(routeObj.get("budget").getAsInt());
                    } else {
                        route.setTotalBudget(5000);
                    }
                    
                    if (routeObj.has("score")) {
                        route.setRecommendationScore(routeObj.get("score").getAsInt());
                    } else {
                        route.setRecommendationScore(85);
                    }
                    
                    if (routeObj.has("dailyItinerary")) {
                        route.setDailyItinerary(routeObj.get("dailyItinerary").toString());
                        
                        // 尝试从第一天的第一个景点获取图片
                        String photoUrl = extractCoverPhoto(routeObj, finalProject);
                        if (photoUrl != null) {
                            route.setCoverImageUrl(photoUrl);
                        }
                    }
                    
                    savedRoutes.add(aiGeneratedRouteRepository.save(route));
                }
            }
            
        } catch (Exception e) {
            // 如果JSON解析失败，创建3个默认路线
            System.err.println("解析AI响应失败: " + e.getMessage());
            e.printStackTrace();
            System.err.println("AI原始响应: " + aiResponse);
            
            // 创建默认的示例行程数据（根据项目天数）
            String destination = finalProject != null ? finalProject.getDestination() : "当地";
            int days = finalProject != null ? finalProject.getDays() : 3;
            
            StringBuilder itineraryBuilder = new StringBuilder("[");
            for (int d = 1; d <= days; d++) {
                if (d > 1) itineraryBuilder.append(",");
                itineraryBuilder.append("{\"day\":" + d + ",\"activities\":[");
                itineraryBuilder.append("\"08:00-09:00 早餐：酒店自助早餐\",");
                itineraryBuilder.append("\"09:30-12:00 上午：参观" + destination + "著名景点\",");
                itineraryBuilder.append("\"12:00-13:30 午餐：品尝" + destination + "特色美食\",");
                itineraryBuilder.append("\"14:00-17:00 下午：继续游览其他景点\",");
                itineraryBuilder.append("\"17:30-19:00 晚餐：特色餐厅\",");
                itineraryBuilder.append("\"19:30-21:00 晚上：休闲活动\"");
                itineraryBuilder.append("]}");
            }
            itineraryBuilder.append("]");
            String defaultItinerary = itineraryBuilder.toString();
            
            // 尝试获取目的地的默认图片（为每个方案准备不同的关键词）
            String[] photoKeywords = {
                finalProject.getDestination() + "旅游",
                finalProject.getDestination() + "风景",
                finalProject.getDestination() + "景点"
            };
            
            for (int i = 0; i < 3; i++) {
                AiGeneratedRoute route = new AiGeneratedRoute();
                route.setProjectId(projectId);
                route.setRouteContent(aiResponse);
                route.setGeneratedTime(LocalDateTime.now());
                route.setRouteTitle("方案" + (i + 1));
                route.setRouteTag("AI生成");
                route.setAttractionsCount(10);
                route.setRestaurantsCount(6);
                route.setTransportMode("公共交通");
                route.setTotalBudget(5000);
                route.setRecommendationScore(85);
                route.setDailyItinerary(defaultItinerary);
                
                // 为每个方案尝试获取不同的图片
                if (finalProject != null) {
                    try {
                        String coverUrl = amapPoiService.getPoiPhotoUrl(
                            photoKeywords[i], 
                            finalProject.getDestination()
                        );
                        if (coverUrl != null) {
                            route.setCoverImageUrl(coverUrl);
                        }
                    } catch (Exception photoEx) {
                        System.err.println("获取方案" + (i+1) + "封面图片失败: " + photoEx.getMessage());
                    }
                }
                
                savedRoutes.add(aiGeneratedRouteRepository.save(route));
            }
        }
        
        return savedRoutes;
    }
    
    /**
     * Extract JSON content from AI response
     */
    private String extractJsonFromResponse(String response) {
        if (response == null) return "{}";
        
        // Try to find JSON in markdown code block
        if (response.contains("```json")) {
            int startIdx = response.indexOf("```json") + 7;
            int endIdx = response.indexOf("```", startIdx);
            if (endIdx > startIdx) {
                return response.substring(startIdx, endIdx).trim();
            }
        } else if (response.contains("```")) {
            int startIdx = response.indexOf("```") + 3;
            int endIdx = response.indexOf("```", startIdx);
            if (endIdx > startIdx) {
                String content = response.substring(startIdx, endIdx).trim();
                // Remove language identifier if present
                if (content.startsWith("json")) {
                    content = content.substring(4).trim();
                }
                return content;
            }
        }
        
        // Try to find JSON object directly
        int jsonStart = response.indexOf("{");
        int jsonEnd = response.lastIndexOf("}");
        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            return response.substring(jsonStart, jsonEnd + 1);
        }
        
        return response;
    }
    
    /**
     * Extract cover photo from route itinerary
     */
    private String extractCoverPhoto(JsonObject routeObj, TravelProject project) {
        try {
            if (routeObj.has("dailyItinerary")) {
                JsonArray dailyItinerary = routeObj.getAsJsonArray("dailyItinerary");
                List<String> photoUrls = new ArrayList<>();
                
                // 从所有天的行程中提取景点图片
                for (int i = 0; i < dailyItinerary.size(); i++) {
                    JsonObject dayObj = dailyItinerary.get(i).getAsJsonObject();
                    if (dayObj.has("activities")) {
                        JsonArray activities = dayObj.getAsJsonArray("activities");
                        for (int j = 0; j < activities.size(); j++) {
                            String activity = activities.get(j).getAsString();
                            // 只提取景点相关的活动（不包括餐饮）
                            if (activity.contains("景点：") || activity.contains("上午：") || 
                                activity.contains("下午：") || activity.contains("晚上：")) {
                                String poiName = extractPoiName(activity);
                                if (poiName != null && !poiName.isEmpty() && project != null) {
                                    try {
                                        String photoUrl = amapPoiService.getPoiPhotoUrl(poiName, project.getDestination());
                                        if (photoUrl != null && !photoUrls.contains(photoUrl)) {
                                            photoUrls.add(photoUrl);
                                            // 最多收集6张图片
                                            if (photoUrls.size() >= 6) {
                                                break;
                                            }
                                        }
                                    } catch (Exception e) {
                                        System.err.println("获取景点图片失败: " + poiName + " - " + e.getMessage());
                                    }
                                }
                            }
                        }
                        if (photoUrls.size() >= 6) break;
                    }
                }
                
                // 如果有图片，返回合并URL（使用逗号分隔）
                if (!photoUrls.isEmpty()) {
                    return String.join(",", photoUrls);
                }
            }
        } catch (Exception e) {
            System.err.println("获取封面图片失败: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Extract POI name from activity string
     * Example: "08:00-09:00 早餐：推荐餐厅" -> "推荐餐厅"
     * Example: "09:30-12:00 上午：参观西湖" -> "西湖"
     */
    private String extractPoiName(String activity) {
        if (activity == null || activity.isEmpty()) {
            return null;
        }
        
        // 移除时间段 (HH:MM-HH:MM)
        activity = activity.replaceAll("^\\d{2}:\\d{2}-\\d{2}:\\d{2}\\s*", "");
        
        // 移除常见的时间前缀
        activity = activity.replaceAll("^[上下]午[:：\\s]*", "");
        activity = activity.replaceAll("^晚上[:：\\s]*", "");
        activity = activity.replaceAll("^中午[:：\\s]*", "");
        activity = activity.replaceAll("^早餐[:：\\s]*", "");
        activity = activity.replaceAll("^午餐[:：\\s]*", "");
        activity = activity.replaceAll("^晚餐[:：\\s]*", "");
        
        // 移除常见动词
        activity = activity.replaceAll("^[参观游览前往体验品尝打卡]+", "");
        
        // 取第一个逗号或句号之前的内容
        if (activity.contains(",")) {
            activity = activity.split(",")[0];
        }
        if (activity.contains("，")) {
            activity = activity.split("，")[0];
        }
        if (activity.contains("。")) {
            activity = activity.split("。")[0];
        }
        
        return activity.trim();
    }
}