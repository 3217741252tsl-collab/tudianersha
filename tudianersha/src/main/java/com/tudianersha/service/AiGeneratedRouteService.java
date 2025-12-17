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
    
    @Autowired
    private AmapDirectionService amapDirectionService;
    
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
        
        // 打印Kimi返回的原始响应，检查是否包含duration和ticket
        System.out.println("\n========== Kimi AI 原始响应 ==========");
        System.out.println(aiResponse);
        System.out.println("========== 响应结束 ==========\n");
        
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
        
        // 出发时间
        String departureTime = project.getDepartureTime();
        if (departureTime != null && !departureTime.isEmpty()) {
            prompt.append("出发时间：").append(departureTime).append("\n");
        } else {
            prompt.append("出发时间：08:00\n");
            departureTime = "08:00";
        }
        
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
        
        // Requirements for AI - 生成带游玩时间和门票的景点列表
        prompt.append("\n\n请生成3条不同风格的路线方案，每条路线包含：\n");
        prompt.append("1. 路线名称\n");
        prompt.append("2. 路线特色标签\n");
        prompt.append("3. 每天的景点列表（每天3-4个景点，包含景点名称、建议游玩时间、门票价格）\n");
        prompt.append("4. 预算估算（交通费用、门票费用、总计）\n");
        prompt.append("5. 推荐指数\n\n");
        
        prompt.append("请严格按照以下JSON格式返回3条路线：\n");
        prompt.append("```json\n");
        prompt.append("{\n");
        prompt.append("  \"routes\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"title\": \"路线名称\",\n");
        prompt.append("      \"tag\": \"特色标签\",\n");
        prompt.append("      \"score\": 85,\n");
        prompt.append("      \"estimatedBudget\": {\n");
        prompt.append("        \"transport\": 50,\n");
        prompt.append("        \"tickets\": 70,\n");
        prompt.append("        \"total\": 120\n");
        prompt.append("      },\n");
        prompt.append("      \"dailyItinerary\": [\n");
        prompt.append("        {\n");
        prompt.append("          \"day\": 1,\n");
        prompt.append("          \"attractions\": [\n");
        prompt.append("            {\"name\": \"可园\", \"duration\": \"1.5小时\", \"ticket\": 8},\n");
        prompt.append("            {\"name\": \"东莞展览馆\", \"duration\": \"1小时\", \"ticket\": 0},\n");
        prompt.append("            {\"name\": \"人民公园\", \"duration\": \"2小时\", \"ticket\": 0}\n");
        prompt.append("          ]\n");
        prompt.append("        }\n");
        prompt.append("      ]\n");
        prompt.append("    }\n");
        prompt.append("  ]\n");
        prompt.append("}\n");
        prompt.append("```\n");
        prompt.append("\n重要要求（必须严格遵守）：\n");
        prompt.append("1. 景点格式要求：\n");
        prompt.append("   - 每个景点必须是对象格式：{\"name\": \"景点名\", \"duration\": \"1.5小时\", \"ticket\": 8}\n");
        prompt.append("   - name: 景点官方名称（高德地图可识别）\n");
        prompt.append("   - duration: 建议游玩时间（简化格式如'1.5小时'、'2小时'、'半小时'）\n");
        prompt.append("   - ticket: 门票价格（数字，免费为0）\n");
        prompt.append("2. 禁止包含以下内容：\n");
        prompt.append("   - 禁止包含时间安排（如'09:00-11:00'）\n");
        prompt.append("   - 禁止包含餐饮推荐（如'早餐'、'午餐'、'晚餐'）\n");
        prompt.append("   - 禁止包含交通描述（如'乘车前往'、'步行到'）\n");
        prompt.append("   - 禁止包含活动描述（如'游览'、'参观'、'拍照'）\n");
        prompt.append("3. 景点必须是高德地图可识别的真实景点：\n");
        prompt.append("   - 使用官方名称（如'可园'、'东莞展览馆'、'虎门销烟纪念馆'）\n");
        prompt.append("   - 禁止使用模糊名称（如'当地商业街'、'美食街'、'公园'）\n");
        prompt.append("   - 禁止使用不存在的景点\n");
        prompt.append("4. 景点距离要求（非常重要）：\n");
        prompt.append("   - 相邻景点之间距离必须在10公里以内，最好在5公里以内\n");
        prompt.append("   - 同一天的景点必须在同一区域\n");
        prompt.append("   - 禁止跨区域安排景点\n");
        prompt.append("5. 每天3-4个景点，同一景点不要重复\n");
        prompt.append("6. 预算估算要求：\n");
        prompt.append("   - transport: 预估全程交通费用（公交/地铁/出租车费用）\n");
        prompt.append("   - tickets: 所有景点门票费用总和\n");
        prompt.append("   - total: transport + tickets\n");
        
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
                    // 解析新的预算估算字段
                    if (routeObj.has("estimatedBudget")) {
                        JsonObject budgetObj = routeObj.getAsJsonObject("estimatedBudget");
                        int transport = budgetObj.has("transport") ? budgetObj.get("transport").getAsInt() : 0;
                        int tickets = budgetObj.has("tickets") ? budgetObj.get("tickets").getAsInt() : 0;
                        int total = budgetObj.has("total") ? budgetObj.get("total").getAsInt() : (transport + tickets);
                        route.setTotalBudget(total);
                        // 将详细预算信息存储到budgetsJson中，前端可以解析
                        route.setBudgetsJson(budgetObj.toString());
                    }
                    if (routeObj.has("score")) {
                        route.setRecommendationScore(routeObj.get("score").getAsInt());
                    }
                    if (routeObj.has("dailyItinerary")) {
                        JsonArray originalItinerary = routeObj.getAsJsonArray("dailyItinerary");
                        
                        // 将Kimi生成的景点名称转换为带有高德POI信息的结构化数据
                        // 传入出发地坐标，用于计算第一天的交通
                        String departureCoord = finalProject.getDepartureLocationCoord();
                        String departureName = finalProject.getDepartureLocation();
                        JsonArray enrichedItinerary = enrichItineraryWithAmapData(originalItinerary, finalProject.getDestination(), departureCoord, departureName);
                        
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
                                "https://images.unsplash.com/photo-1476514525535-07fb3b4ae5f1?w=800&h=400&fit=crop",
                                "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?w=800&h=400&fit=crop"
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
     * 将Kimi生成的行程数据丰富为带有高德POI信息和交通信息的结构化数据
     * 新格式: [{"day": 1, "attractions": ["景点A", "景点B", ...]}, ...]
     * 目标格式: [{"day": 1, "activities": [{"text": "景点A", "poiInfo": {...}}, ...], "transports": [{...}]}, ...]
     */
    private JsonArray enrichItineraryWithAmapData(JsonArray originalItinerary, String city, String departureCoord, String departureName) {
        JsonArray enrichedItinerary = new JsonArray();
        
        try {
            for (int dayIdx = 0; dayIdx < originalItinerary.size(); dayIdx++) {
                JsonObject originalDay = originalItinerary.get(dayIdx).getAsJsonObject();
                JsonObject enrichedDay = new JsonObject();
                
                // 复制day字段
                if (originalDay.has("day")) {
                    enrichedDay.add("day", originalDay.get("day"));
                }
                
                // 用于存储当天所有有效景点的位置信息
                List<JsonObject> activitiesWithLocation = new ArrayList<>();
                JsonArray enrichedActivities = new JsonArray();
                
                // 检查是新格式(attractions)还是旧格式(activities)
                if (originalDay.has("attractions") && originalDay.get("attractions").isJsonArray()) {
                    // 新格式：景点对象列表 {name, duration, ticket} 或 纯字符串列表
                    JsonArray attractions = originalDay.getAsJsonArray("attractions");
                    
                    for (int actIdx = 0; actIdx < attractions.size(); actIdx++) {
                        JsonElement attractionElement = attractions.get(actIdx);
                        String attractionName;
                        String duration = null;
                        int ticket = 0;
                        
                        // 支持两种格式：对象格式和字符串格式
                        if (attractionElement.isJsonObject()) {
                            JsonObject attrObj = attractionElement.getAsJsonObject();
                            attractionName = attrObj.has("name") ? attrObj.get("name").getAsString().trim() : "";
                            duration = attrObj.has("duration") ? attrObj.get("duration").getAsString() : null;
                            ticket = attrObj.has("ticket") ? attrObj.get("ticket").getAsInt() : 0;
                        } else {
                            // 兑容旧的字符串格式
                            attractionName = attractionElement.getAsString().trim();
                        }
                        
                        if (attractionName.isEmpty()) continue;
                        
                        JsonObject enrichedActivity = new JsonObject();
                        enrichedActivity.addProperty("text", attractionName);
                        enrichedActivity.addProperty("index", actIdx);
                        enrichedActivity.addProperty("isMeal", false);
                        
                        // 添加游玩时间和门票信息
                        if (duration != null) {
                            enrichedActivity.addProperty("duration", duration);
                        }
                        enrichedActivity.addProperty("ticket", ticket);
                        
                        // 调用高德API获取POI详细信息
                        boolean hasValidLocation = false;
                        try {
                            Map<String, Object> poiInfo = amapPoiService.searchPoiWithPhotos(attractionName, city);
                            
                            if (poiInfo != null && !poiInfo.isEmpty()) {
                                JsonObject poiInfoJson = gson.toJsonTree(poiInfo).getAsJsonObject();
                                enrichedActivity.add("poiInfo", poiInfoJson);
                                
                                if (poiInfo.containsKey("location")) {
                                    String location = poiInfo.get("location").toString();
                                    String name = poiInfo.containsKey("name") ? poiInfo.get("name").toString() : attractionName;
                                    
                                    enrichedActivity.addProperty("location", location);
                                    enrichedActivity.addProperty("poiName", name);
                                    
                                    activitiesWithLocation.add(enrichedActivity);
                                    hasValidLocation = true;
                                }
                                
                                System.out.println("[丰富行程] 景点 '" + attractionName + "' 获取高德数据成功" + 
                                    (duration != null ? ", 游玩时间:" + duration : "") + 
                                    ", 门票:¥" + ticket);
                            } else {
                                System.out.println("[丰富行程] 景点 '" + attractionName + "' 未找到高德数据");
                            }
                        } catch (Exception e) {
                            System.err.println("[丰富行程] 获取 '" + attractionName + "' 高德数据失败: " + e.getMessage());
                        }
                        
                        // 只有获取到有效位置的景点才添加到行程中
                        if (hasValidLocation) {
                            enrichedActivities.add(enrichedActivity);
                            System.out.println("[丰富行程] ✓ 景点 '" + attractionName + "' 已添加到行程");
                        } else {
                            System.out.println("[丰富行程] ✗ 景点 '" + attractionName + "' 无有效位置信息，已过滤");
                        }
                    }
                } else if (originalDay.has("activities") && originalDay.get("activities").isJsonArray()) {
                    // 旧格式兼容：activities字符串数组
                    JsonArray originalActivities = originalDay.getAsJsonArray("activities");
                    
                    String lastLocationWithCoord = null;
                    String lastPoiName = null;
                    
                    for (int actIdx = 0; actIdx < originalActivities.size(); actIdx++) {
                        String activityText = originalActivities.get(actIdx).getAsString();
                        JsonObject enrichedActivity = new JsonObject();
                        enrichedActivity.addProperty("text", activityText);
                        enrichedActivity.addProperty("index", actIdx);
                        
                        String poiName = extractPoiName(activityText);
                        // 所有活动都视为景点（已移除餐饮功能）
                        enrichedActivity.addProperty("isMeal", false);
                        
                        boolean hasLocation = false;
                        if (poiName != null && !poiName.isEmpty() && poiName.length() > 1) {
                            try {
                                Map<String, Object> poiInfo = amapPoiService.searchPoiWithPhotos(poiName, city);
                                
                                if (poiInfo != null && !poiInfo.isEmpty()) {
                                    JsonObject poiInfoJson = gson.toJsonTree(poiInfo).getAsJsonObject();
                                    enrichedActivity.add("poiInfo", poiInfoJson);
                                    
                                    if (poiInfo.containsKey("location")) {
                                        String location = poiInfo.get("location").toString();
                                        String name = poiInfo.containsKey("name") ? poiInfo.get("name").toString() : poiName;
                                        
                                        enrichedActivity.addProperty("location", location);
                                        enrichedActivity.addProperty("poiName", name);
                                        hasLocation = true;
                                        
                                        lastLocationWithCoord = location;
                                        lastPoiName = name;
                                        
                                        activitiesWithLocation.add(enrichedActivity);
                                    }
                                    
                                    System.out.println("[丰富行程] 景点 '" + poiName + "' 获取高德数据成功");
                                }
                            } catch (Exception e) {
                                System.err.println("[丰富行程] 获取 '" + poiName + "' 高德数据失败: " + e.getMessage());
                            }
                        }
                        
                        enrichedActivities.add(enrichedActivity);
                    }
                }
                
                enrichedDay.add("activities", enrichedActivities);
                
                // 计算相邻景点之间的交通信息
                JsonArray transportsArray = calculateTransportsBetweenActivities(activitiesWithLocation, city);
                
                // 第一天：添加从出发地到第一个景点的交通
                if (dayIdx == 0 && departureCoord != null && !departureCoord.isEmpty() && !activitiesWithLocation.isEmpty()) {
                    try {
                        JsonObject firstActivity = activitiesWithLocation.get(0);
                        String firstLocation = firstActivity.get("location").getAsString();
                        String firstName = firstActivity.has("poiName") ? firstActivity.get("poiName").getAsString() : "第一个景点";
                        int firstOriginalIndex = firstActivity.get("index").getAsInt();
                        
                        Map<String, Object> departureTransport = amapDirectionService.getTransportInfo(departureCoord, firstLocation, city, firstName);
                        
                        if (departureTransport != null && !departureTransport.isEmpty()) {
                            JsonObject transportJson = gson.toJsonTree(departureTransport).getAsJsonObject();
                            transportJson.addProperty("fromIndex", -1);
                            transportJson.addProperty("toIndex", firstOriginalIndex);
                            transportJson.addProperty("fromName", departureName != null ? departureName : "出发地");
                            transportJson.addProperty("toName", firstName);
                            transportJson.addProperty("isDeparture", true);
                            
                            JsonArray newTransports = new JsonArray();
                            newTransports.add(transportJson);
                            for (int t = 0; t < transportsArray.size(); t++) {
                                newTransports.add(transportsArray.get(t));
                            }
                            transportsArray = newTransports;
                            
                            System.out.println("[出发地交通] 添加从 '" + departureName + "' 到 '" + firstName + "' 的交通信息");
                        }
                    } catch (Exception e) {
                        System.err.println("[出发地交通] 计算失败: " + e.getMessage());
                    }
                }
                
                if (transportsArray.size() > 0) {
                    enrichedDay.add("transports", transportsArray);
                    System.out.println("[交通信息] 第" + (dayIdx + 1) + "天添加了" + transportsArray.size() + "条交通信息");
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
     * 计算相邻景点之间的交通信息
     */
    private JsonArray calculateTransportsBetweenActivities(List<JsonObject> activitiesWithLocation, String city) {
        JsonArray transportsArray = new JsonArray();
        
        for (int i = 0; i < activitiesWithLocation.size() - 1; i++) {
            JsonObject fromActivity = activitiesWithLocation.get(i);
            JsonObject toActivity = activitiesWithLocation.get(i + 1);
            
            String fromLocation = fromActivity.get("location").getAsString();
            String toLocation = toActivity.get("location").getAsString();
            String fromName = fromActivity.has("poiName") ? fromActivity.get("poiName").getAsString() : "起点";
            String toName = toActivity.has("poiName") ? toActivity.get("poiName").getAsString() : "终点";
            int fromIndex = fromActivity.get("index").getAsInt();
            int toIndex = toActivity.get("index").getAsInt();
            
            try {
                System.out.println("[交通规划] 计算从 '" + fromName + "' 到 '" + toName + "' 的交通信息");
                
                Map<String, Object> transportInfo = amapDirectionService.getTransportInfo(fromLocation, toLocation, city, toName);
                
                if (transportInfo != null && !transportInfo.isEmpty()) {
                    JsonObject transportJson = new JsonObject();
                    transportJson.addProperty("fromIndex", fromIndex);
                    transportJson.addProperty("toIndex", toIndex);
                    transportJson.addProperty("fromName", fromName);
                    transportJson.addProperty("toName", toName);
                    
                    // 添加交通信息字段
                    if (transportInfo.containsKey("method")) {
                        transportJson.addProperty("method", transportInfo.get("method").toString());
                    }
                    if (transportInfo.containsKey("recommendedMethod")) {
                        transportJson.addProperty("recommendedMethod", transportInfo.get("recommendedMethod").toString());
                    }
                    if (transportInfo.containsKey("distance")) {
                        transportJson.addProperty("distance", ((Number) transportInfo.get("distance")).intValue());
                    }
                    if (transportInfo.containsKey("distanceText")) {
                        transportJson.addProperty("distanceText", transportInfo.get("distanceText").toString());
                    }
                    if (transportInfo.containsKey("duration")) {
                        transportJson.addProperty("duration", ((Number) transportInfo.get("duration")).intValue());
                    }
                    if (transportInfo.containsKey("durationText")) {
                        transportJson.addProperty("durationText", transportInfo.get("durationText").toString());
                    }
                    if (transportInfo.containsKey("cost")) {
                        transportJson.addProperty("cost", ((Number) transportInfo.get("cost")).intValue());
                    }
                    if (transportInfo.containsKey("costText")) {
                        transportJson.addProperty("costText", transportInfo.get("costText").toString());
                    }
                    if (transportInfo.containsKey("details")) {
                        transportJson.addProperty("details", transportInfo.get("details").toString());
                    }
                    if (transportInfo.containsKey("warning")) {
                        transportJson.addProperty("warning", transportInfo.get("warning").toString());
                    }
                    if (transportInfo.containsKey("distanceTooFar")) {
                        transportJson.addProperty("distanceTooFar", (Boolean) transportInfo.get("distanceTooFar"));
                    }
                    
                    // 添加分步交通信息（steps数组）
                    if (transportInfo.containsKey("steps")) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> steps = (List<Map<String, Object>>) transportInfo.get("steps");
                        if (steps != null && !steps.isEmpty()) {
                            JsonArray stepsArray = new Gson().toJsonTree(steps).getAsJsonArray();
                            transportJson.add("steps", stepsArray);
                        }
                    }
                    
                    transportsArray.add(transportJson);
                    
                    System.out.println("[交通规划] ✓ 成功: " + fromName + " → " + toName + 
                        " | " + transportInfo.getOrDefault("distanceText", "未知") + 
                        " | " + transportInfo.getOrDefault("durationText", "未知") + 
                        " | " + transportInfo.getOrDefault("costText", "未知"));
                }
            } catch (Exception e) {
                System.err.println("[交通规划] ✗ 失败: " + fromName + " → " + toName + " | " + e.getMessage());
            }
        }
        
        return transportsArray;
    }
    
    /**
     * Extract POI name from activity string
     * Example: "09:30-12:00 上午：参观西湖" -> "西湖"
     * Example: "景点：东莞博物馆" -> "东莞博物馆"
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
        activity = activity.replaceAll("^景点[:：\\s]*", "");
        
        // 移除常见动词
        activity = activity.replaceAll("^[参观游览前往体验打卡]+", "");
        
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
            activities.add("09:00-11:00 上午：参观当地热门景点");
            activities.add("11:30-14:00 中午：游览文化景区");
            activities.add("14:30-17:00 下午：深度游览景区，欣赏自然风光");
            activities.add("17:30-19:30 晚上：漫步街头或观看夜景");
            
            dayObj.add("activities", activities);
            itinerary.add(dayObj);
        }
        
        return itinerary;
    }
}