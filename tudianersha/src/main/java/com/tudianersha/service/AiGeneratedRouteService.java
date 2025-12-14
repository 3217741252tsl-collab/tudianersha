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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AiGeneratedRouteService {
    
    private final Gson gson = new Gson();
    
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
        prompt.append("            \"08:00-09:00 早餐：推荐餐厅名称，品尝当地特色美食\",\n");
        prompt.append("            \"09:30-12:00 上午：参观景点名称，游玩时长约2.5小时\",\n");
        prompt.append("            \"12:00-13:30 午餐：餐厅名称，人均消费XX元\",\n");
        prompt.append("            \"14:00-17:00 下午：前往景点名称，深度游览\",\n");
        prompt.append("            \"17:30-19:00 晚餐：特色餐厅，品尝XXX\",\n");
        prompt.append("            \"19:30-21:00 晚上：夜游景点或休闲活动\"\n");
        prompt.append("          ]\n");
        prompt.append("        }\n");
        prompt.append("      ]\n");
        prompt.append("    }\n");
        prompt.append("  ]\n");
        prompt.append("}\n");
        prompt.append("```\n");
        prompt.append("\n重要要求：\n");
        prompt.append("1. 每个活动必须包含具体的时间段（如：08:00-09:00）\n");
        prompt.append("2. 活动描述要具体，包括景点名称、活动内容、预计时长\n");
        prompt.append("3. 合理安排早中晚三餐时间\n");
        prompt.append("4. 考虑景点之间的交通时间\n");
        prompt.append("5. 每天安排6-8个时间段的活动\n");
        prompt.append("6. 景点和餐厅名称要真实存在\n");
        
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
            String jsonContent = aiResponse;
            
            System.out.println("[解析] 原始AI响应长度: " + aiResponse.length());
            System.out.println("[解析] AI响应前200字符: " + aiResponse.substring(0, Math.min(200, aiResponse.length())));
            
            if (aiResponse.contains("```json")) {
                int startIdx = aiResponse.indexOf("```json") + 7;
                int endIdx = aiResponse.indexOf("```", startIdx);
                if (endIdx > startIdx) {
                    jsonContent = aiResponse.substring(startIdx, endIdx).trim();
                    System.out.println("[解析] 从```json代码块提取JSON，长度: " + jsonContent.length());
                }
            } else if (aiResponse.contains("```")) {
                int startIdx = aiResponse.indexOf("```") + 3;
                int endIdx = aiResponse.indexOf("```", startIdx);
                if (endIdx > startIdx) {
                    jsonContent = aiResponse.substring(startIdx, endIdx).trim();
                    System.out.println("[解析] 从```代码块提取JSON，长度: " + jsonContent.length());
                }
            } else {
                // 尝试寻找第一个 { 和最后一个 }
                int firstBrace = aiResponse.indexOf('{');
                int lastBrace = aiResponse.lastIndexOf('}');
                if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
                    jsonContent = aiResponse.substring(firstBrace, lastBrace + 1).trim();
                    System.out.println("[解析] 从花括号提取JSON，长度: " + jsonContent.length());
                }
            }
            
            System.out.println("[解析] 提取的JSON前200字符: " + jsonContent.substring(0, Math.min(200, jsonContent.length())));
            System.out.println("[解析] 提取的JSON后200字符: " + jsonContent.substring(Math.max(0, jsonContent.length() - 200)));
            
            // 验证JSON是否以{开头并以}结尾
            if (!jsonContent.trim().startsWith("{")) {
                throw new IllegalArgumentException("AI响应不是有效的JSON对象（缺少开头的{），内容: " + jsonContent.substring(0, Math.min(100, jsonContent.length())));
            }
            if (!jsonContent.trim().endsWith("}")) {
                System.err.println("[警告] AI响应JSON不完整（缺少结尾的}），可能被截断。请检查max_tokens设置。");
                System.err.println("[警告] JSON结尾内容: " + jsonContent.substring(Math.max(0, jsonContent.length() - 200)));
                throw new IllegalArgumentException("AI响应JSON不完整（缺少结尾的}），响应长度: " + jsonContent.length() + " 字符。请增加max_tokens参数。");
            }
            
            // Parse JSON
            JsonObject jsonResponse = gson.fromJson(jsonContent, JsonObject.class);
            
            if (jsonResponse.has("routes")) {
                JsonArray routes = jsonResponse.getAsJsonArray("routes");
                
                for (int i = 0; i < Math.min(3, routes.size()); i++) {
                    JsonObject routeObj = routes.get(i).getAsJsonObject();
                    
                    AiGeneratedRoute route = new AiGeneratedRoute();
                    route.setProjectId(projectId);
                    route.setGeneratedTime(LocalDateTime.now());
                    route.setRouteContent(aiResponse); // 保存完整响应
                    
                    // 提取结构化字段
                    if (routeObj.has("title")) {
                        route.setRouteTitle(routeObj.get("title").getAsString());
                    }
                    if (routeObj.has("tag")) {
                        route.setRouteTag(routeObj.get("tag").getAsString());
                    }
                    if (routeObj.has("attractions")) {
                        route.setAttractionsCount(routeObj.get("attractions").getAsInt());
                    }
                    if (routeObj.has("restaurants")) {
                        route.setRestaurantsCount(routeObj.get("restaurants").getAsInt());
                    }
                    if (routeObj.has("transport")) {
                        route.setTransportMode(routeObj.get("transport").getAsString());
                    }
                    if (routeObj.has("budget")) {
                        route.setTotalBudget(routeObj.get("budget").getAsInt());
                    }
                    if (routeObj.has("score")) {
                        route.setRecommendationScore(routeObj.get("score").getAsInt());
                    }
                    if (routeObj.has("dailyItinerary")) {
                        JsonArray originalItinerary = routeObj.getAsJsonArray("dailyItinerary");
                        
                        // 将Kimi生成的景点名称转换为带有高德POI信息的结构化数据
                        JsonArray enrichedItinerary = enrichItineraryWithAmapData(originalItinerary, finalProject.getDestination());
                        
                        route.setDailyItinerary(enrichedItinerary.toString());
                        
                        // 尝试从每条路线的不同景点获取封面图（根据路线索引选择不同景点）
                        try {
                            boolean foundCover = false;
                            int targetActivityIndex = i; // 每条路线从不同的景点开始
                            int checkedCount = 0;
                            
                            // 遍历所有天数和活动，寻找合适的封面图片
                            outerLoop:
                            for (int dayIdx = 0; dayIdx < enrichedItinerary.size(); dayIdx++) {
                                JsonObject dayObj = enrichedItinerary.get(dayIdx).getAsJsonObject();
                                if (dayObj.has("activities")) {
                                    JsonArray activities = dayObj.getAsJsonArray("activities");
                                    for (int actIdx = 0; actIdx < activities.size(); actIdx++) {
                                        JsonObject activityObj = activities.get(actIdx).getAsJsonObject();
                                        if (activityObj.has("poiInfo")) {
                                            JsonObject poiInfo = activityObj.getAsJsonObject("poiInfo");
                                            
                                            // 跳过前面的景点，为每条路线选择不同的封面
                                            if (checkedCount < targetActivityIndex) {
                                                checkedCount++;
                                                continue;
                                            }
                                            
                                            System.out.println("[路线" + (i+1) + "封面图片] 检查POI: " + (poiInfo.has("name") ? poiInfo.get("name").getAsString() : "未知"));
                                            
                                            // 检查photos字段（可能是Array或其他类型）
                                            if (poiInfo.has("photos")) {
                                                JsonElement photosElement = poiInfo.get("photos");
                                                System.out.println("[路线" + (i+1) + "封面图片] photos字段类型: " + photosElement.getClass().getSimpleName());
                                                
                                                JsonArray photos = null;
                                                if (photosElement.isJsonArray()) {
                                                    photos = photosElement.getAsJsonArray();
                                                } else if (photosElement.isJsonObject()) {
                                                    // 如果是对象，尝试转换
                                                    System.out.println("[路线" + (i+1) + "封面图片] photos是JsonObject，尝试其他方式");
                                                    checkedCount++;
                                                    continue;
                                                }
                                                
                                                if (photos != null && photos.size() > 0) {
                                                    System.out.println("[路线" + (i+1) + "封面图片] 找到 " + photos.size() + " 张图片");
                                                    JsonElement firstPhotoElement = photos.get(0);
                                                    
                                                    if (firstPhotoElement.isJsonObject()) {
                                                        JsonObject firstPhoto = firstPhotoElement.getAsJsonObject();
                                                        if (firstPhoto.has("url")) {
                                                            String photoUrl = firstPhoto.get("url").getAsString();
                                                            if (photoUrl != null && !photoUrl.isEmpty()) {
                                                                route.setCoverImageUrl(photoUrl);
                                                                foundCover = true;
                                                                System.out.println("[路线" + (i+1) + "封面图片] ✓ 找到封面: " + photoUrl);
                                                                break outerLoop;
                                                            }
                                                        } else {
                                                            System.out.println("[路线" + (i+1) + "封面图片] 图片对象没有url字段: " + firstPhoto);
                                                        }
                                                    } else {
                                                        System.out.println("[路线" + (i+1) + "封面图片] 第一个photo不是JsonObject: " + firstPhotoElement);
                                                    }
                                                } else {
                                                    System.out.println("[路线" + (i+1) + "封面图片] photos数组为空");
                                                }
                                            } else {
                                                System.out.println("[路线" + (i+1) + "封面图片] POI没有photos字段");
                                            }
                                            checkedCount++;
                                        }
                                    }
                                }
                            }
                            
                            // 如果没有找到封面图，使用不同的默认图片
                            if (!foundCover) {
                                System.out.println("[路线" + (i+1) + "封面图片] ✗ 未找到带图片的景点，使用默认图片");
                                // 为每条路线使用不同的默认图片
                                String[] defaultCovers = {
                                    "https://images.unsplash.com/photo-1530521954074-e64f6810b32d?w=800&h=400&fit=crop",
                                    "https://images.unsplash.com/photo-1476514525535-07fb3b4ae5f1?w=800&h=400&fit=crop",
                                    "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?w=800&h=400&fit=crop"
                                };
                                int coverIndex = i % defaultCovers.length; // 使用路线索引而不是ID
                                route.setCoverImageUrl(defaultCovers[coverIndex]);
                                System.out.println("[路线" + (i+1) + "封面图片] 设置默认封面[" + coverIndex + "]: " + defaultCovers[coverIndex]);
                            }
                        } catch (Exception e) {
                            System.err.println("[路线" + (i+1) + "封面图片] ✗ 获取封面图片失败: " + e.getMessage());
                            e.printStackTrace();
                            // 发生异常时也根据索引设置不同的默认图片
                            String[] defaultCovers = {
                                "https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?w=800&h=400&fit=crop",
                                "https://images.unsplash.com/photo-1530521954074-e64f6810b32d?w=800&h=400&fit=crop",
                                "https://images.unsplash.com/photo-1476514525535-07fb3b4ae5f1?w=800&h=400&fit=crop"
                            };
                            int coverIndex = i % defaultCovers.length;
                            route.setCoverImageUrl(defaultCovers[coverIndex]);
                            System.out.println("[路线" + (i+1) + "封面图片] 异常后设置默认封面[" + coverIndex + "]: " + defaultCovers[coverIndex]);
                        }
                    }
                    
                    savedRoutes.add(aiGeneratedRouteRepository.save(route));
                }
            }
            
        } catch (com.google.gson.JsonSyntaxException e) {
            // JSON语法错误
            System.err.println("[错误] JSON语法错误: " + e.getMessage());
            System.err.println("[错误] AI原始响应: " + aiResponse);
            e.printStackTrace();
            
            // 创建降级的默认路线
            int days = 3;
            if (finalProject != null) {
                days = finalProject.getDays() != null ? finalProject.getDays() : 3;
            }
            
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
                
                // 生成默认的dailyItinerary
                JsonArray defaultItinerary = generateDefaultItinerary(days);
                route.setDailyItinerary(defaultItinerary.toString());
                
                savedRoutes.add(aiGeneratedRouteRepository.save(route));
            }
            
        } catch (IllegalArgumentException e) {
            // 非法参数（比如不是JSON对象）
            System.err.println("[错误] " + e.getMessage());
            System.err.println("[错误] AI原始响应: " + aiResponse);
            
            // 创建降级的默认路线
            int days = 3;
            if (finalProject != null) {
                days = finalProject.getDays() != null ? finalProject.getDays() : 3;
            }
            
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
                
                // 生成默认的dailyItinerary
                JsonArray defaultItinerary = generateDefaultItinerary(days);
                route.setDailyItinerary(defaultItinerary.toString());
                
                savedRoutes.add(aiGeneratedRouteRepository.save(route));
            }
            
        } catch (Exception e) {
            // 其他解析错误
            System.err.println("解析AI响应失败: " + e.getMessage());
            e.printStackTrace();
            
            // 获取项目天数
            int days = 3;
            if (finalProject != null) {
                days = finalProject.getDays() != null ? finalProject.getDays() : 3;
            }
            
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
                
                // 生成默认的dailyItinerary
                JsonArray defaultItinerary = generateDefaultItinerary(days);
                route.setDailyItinerary(defaultItinerary.toString());
                
                savedRoutes.add(aiGeneratedRouteRepository.save(route));
            }
        }
        
        return savedRoutes;
    }
    
    /**
     * 将Kimi生成的行程数据丰富为带有高德POI信息的结构化数据
     * 原始格式: [{"day": 1, "activities": ["08:00-09:00 早餐：老莞城茶餐厅", ...]}, ...]
     * 目标格式: [{"day": 1, "activities": [{"text": "08:00-09:00 早餐：老莞城茶餐厅", "poiInfo": {...}}, ...]}, ...]
     */
    private JsonArray enrichItineraryWithAmapData(JsonArray originalItinerary, String city) {
        JsonArray enrichedItinerary = new JsonArray();
        
        try {
            for (int dayIdx = 0; dayIdx < originalItinerary.size(); dayIdx++) {
                JsonObject originalDay = originalItinerary.get(dayIdx).getAsJsonObject();
                JsonObject enrichedDay = new JsonObject();
                
                // 复制day字段
                if (originalDay.has("day")) {
                    enrichedDay.add("day", originalDay.get("day"));
                }
                
                // 处理activities
                if (originalDay.has("activities") && originalDay.get("activities").isJsonArray()) {
                    JsonArray originalActivities = originalDay.getAsJsonArray("activities");
                    JsonArray enrichedActivities = new JsonArray();
                    
                    for (int actIdx = 0; actIdx < originalActivities.size(); actIdx++) {
                        String activityText = originalActivities.get(actIdx).getAsString();
                        JsonObject enrichedActivity = new JsonObject();
                        enrichedActivity.addProperty("text", activityText);
                        
                        // 提取景点名称
                        String poiName = extractPoiName(activityText);
                        
                        // 跳过餐饮活动（包含"餐"或"食"字的通常不是景点）
                        boolean isMeal = activityText.contains("早餐") || activityText.contains("午餐") || 
                                        activityText.contains("晚餐") || activityText.contains("餐饮");
                        
                        if (!isMeal && poiName != null && !poiName.isEmpty() && poiName.length() > 1) {
                            try {
                                // 调用高德API获取POI详细信息
                                Map<String, Object> poiInfo = amapPoiService.searchPoiWithPhotos(poiName, city);
                                
                                if (poiInfo != null && !poiInfo.isEmpty()) {
                                    // 将Map转换为JsonObject
                                    JsonObject poiInfoJson = gson.toJsonTree(poiInfo).getAsJsonObject();
                                    enrichedActivity.add("poiInfo", poiInfoJson);
                                    
                                    // 输出详细信息
                                    System.out.println("[丰富行程] 景点 '" + poiName + "' 获取高德数据成功: " + 
                                        (poiInfo.containsKey("name") ? poiInfo.get("name") : "Unknown"));
                                    System.out.println("[丰富行程] - 是否有photos字段: " + poiInfo.containsKey("photos"));
                                    if (poiInfo.containsKey("photos")) {
                                        Object photosObj = poiInfo.get("photos");
                                        System.out.println("[丰富行程] - photos类型: " + (photosObj != null ? photosObj.getClass().getName() : "null"));
                                        if (photosObj instanceof List) {
                                            System.out.println("[丰富行程] - photos数量: " + ((List<?>) photosObj).size());
                                        }
                                    }
                                } else {
                                    System.out.println("[丰富行程] 景点 '" + poiName + "' 未找到高德数据");
                                }
                            } catch (Exception e) {
                                System.err.println("[丰富行程] 获取景点 '" + poiName + "' 高德数据失败: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }
                        
                        enrichedActivities.add(enrichedActivity);
                    }
                    
                    enrichedDay.add("activities", enrichedActivities);
                }
                
                enrichedItinerary.add(enrichedDay);
            }
        } catch (Exception e) {
            System.err.println("[丰富行程] 处理失败: " + e.getMessage());
            e.printStackTrace();
            // 如果失败，返回原始数据
            return originalItinerary;
        }
        
        return enrichedItinerary;
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
    
    /**
     * 生成默认的行程数据（用于AI响应解析失败时的降级方案）
     */
    private JsonArray generateDefaultItinerary(int days) {
        JsonArray itinerary = new JsonArray();
        
        for (int day = 1; day <= days; day++) {
            JsonObject dayObj = new JsonObject();
            dayObj.addProperty("day", day);
            
            JsonArray activities = new JsonArray();
            activities.add("08:00-09:00 早餐：推荐当地特色餐厅");
            activities.add("09:30-12:00 上午：参观当地热门景点，体验文化氛围");
            activities.add("12:00-13:30 午餐：品尝地道美食，人均约50元");
            activities.add("14:00-17:00 下午：深度游览景区，欣赏自然风光");
            activities.add("17:30-19:00 晚餐：特色餐厅，品尝当地名菜");
            activities.add("19:30-21:00 晚上：漫步街头或观看夜景");
            
            dayObj.add("activities", activities);
            itinerary.add(dayObj);
        }
        
        return itinerary;
    }
}