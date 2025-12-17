package com.tudianersha.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tudianersha.entity.TransportCard;
import com.tudianersha.repository.TransportCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TransportCardService {
    
    @Autowired
    private TransportCardRepository transportCardRepository;
    
    @Autowired
    private AmapDirectionService amapDirectionService;
    
    private final Gson gson = new Gson();
    
    /**
     * 一键生成交通路线（支持多方案）
     */
    @Transactional
    public TransportCard generateTransport(Long projectId, Integer dayNumber, String fromActivityId, 
                                          String toActivityId, String fromName, String toName, 
                                          String fromLocation, String toLocation, Long userId) {
        
        System.out.println("[交通卡片] 一键生成: " + fromName + " → " + toName);
        
        // 调用高德地图API获取多个方案
        List<Map<String, Object>> plans = amapDirectionService.getMultipleTransitPlans(fromLocation, toLocation);
        
        if (plans == null || plans.isEmpty()) {
            System.err.println("[交通卡片] 高德API返回为空");
            return createPendingCard(projectId, dayNumber, fromActivityId, toActivityId, 
                                   fromName, toName, fromLocation, toLocation, userId);
        }
        
        // 使用第一个方案作为默认方案
        Map<String, Object> primaryPlan = plans.get(0);
        
        // 查询是否已存在
        TransportCard existingCard = transportCardRepository
            .findByProjectIdAndFromActivityIdAndToActivityId(projectId, fromActivityId, toActivityId);
        
        TransportCard card = existingCard != null ? existingCard : new TransportCard();
        
        // 设置基本信息
        card.setProjectId(projectId);
        card.setDayNumber(dayNumber);
        card.setFromActivityId(fromActivityId);
        card.setToActivityId(toActivityId);
        card.setFromName(fromName);
        card.setToName(toName);
        card.setFromLocation(fromLocation);
        card.setToLocation(toLocation);
        card.setStatus("confirmed");
        
        // 设置主方案数据
        card.setMethod((String) primaryPlan.get("method"));
        card.setDistance((Integer) primaryPlan.get("distance"));
        card.setDistanceText((String) primaryPlan.get("distanceText"));
        card.setDuration((Integer) primaryPlan.get("duration"));
        card.setDurationText((String) primaryPlan.get("durationText"));
        card.setCost((Double) primaryPlan.get("cost"));
        card.setCostText((String) primaryPlan.get("costText"));
        card.setStepsJson(gson.toJson(primaryPlan.get("steps")));
        
        // 保存所有备选方案
        if (plans.size() > 1) {
            card.setAlternativePlans(gson.toJson(plans));
        }
        card.setSelectedPlanIndex(0);
        
        if (existingCard == null) {
            card.setCreatedBy(userId);
        }
        card.setUpdatedBy(userId);
        
        return transportCardRepository.save(card);
    }
    
    /**
     * 手动编辑交通卡片
     */
    @Transactional
    public TransportCard manualEdit(Long cardId, String method, Integer duration, Double cost, 
                                    String customSteps, Long userId) {
        
        TransportCard card = transportCardRepository.findById(cardId)
            .orElseThrow(() -> new RuntimeException("交通卡片不存在"));
        
        card.setStatus("confirmed");
        card.setMethod(method);
        card.setDuration(duration);
        card.setDurationText(duration != null ? formatDuration(duration) : null);
        card.setCost(cost);
        card.setCostText(cost != null ? "¥" + cost : null);
        
        // 自定义步骤
        if (customSteps != null && !customSteps.isEmpty()) {
            card.setStepsJson(customSteps);
        }
        
        card.setUpdatedBy(userId);
        
        return transportCardRepository.save(card);
    }
    
    /**
     * 切换方案
     */
    @Transactional
    public TransportCard switchPlan(Long cardId, Integer planIndex, Long userId) {
        
        TransportCard card = transportCardRepository.findById(cardId)
            .orElseThrow(() -> new RuntimeException("交通卡片不存在"));
        
        if (card.getAlternativePlans() == null || card.getAlternativePlans().isEmpty()) {
            throw new RuntimeException("没有备选方案");
        }
        
        List<Map<String, Object>> plans = gson.fromJson(card.getAlternativePlans(), List.class);
        
        if (planIndex < 0 || planIndex >= plans.size()) {
            throw new RuntimeException("方案索引越界");
        }
        
        Map<String, Object> selectedPlan = plans.get(planIndex);
        
        // 更新为选中的方案
        card.setMethod((String) selectedPlan.get("method"));
        card.setDistance(((Number) selectedPlan.get("distance")).intValue());
        card.setDistanceText((String) selectedPlan.get("distanceText"));
        card.setDuration(((Number) selectedPlan.get("duration")).intValue());
        card.setDurationText((String) selectedPlan.get("durationText"));
        card.setCost(((Number) selectedPlan.get("cost")).doubleValue());
        card.setCostText((String) selectedPlan.get("costText"));
        card.setStepsJson(gson.toJson(selectedPlan.get("steps")));
        card.setSelectedPlanIndex(planIndex);
        card.setUpdatedBy(userId);
        
        return transportCardRepository.save(card);
    }
    
    /**
     * 获取项目的所有交通卡片
     */
    public List<TransportCard> getProjectTransportCards(Long projectId) {
        return transportCardRepository.findByProjectIdOrderByDayNumberAsc(projectId);
    }
    
    /**
     * 创建待定卡片
     */
    private TransportCard createPendingCard(Long projectId, Integer dayNumber, String fromActivityId,
                                           String toActivityId, String fromName, String toName,
                                           String fromLocation, String toLocation, Long userId) {
        
        TransportCard card = new TransportCard();
        card.setProjectId(projectId);
        card.setDayNumber(dayNumber);
        card.setFromActivityId(fromActivityId);
        card.setToActivityId(toActivityId);
        card.setFromName(fromName);
        card.setToName(toName);
        card.setFromLocation(fromLocation);
        card.setToLocation(toLocation);
        card.setStatus("pending");
        card.setMethod("待定");
        card.setCreatedBy(userId);
        card.setUpdatedBy(userId);
        
        return transportCardRepository.save(card);
    }
    
    /**
     * 格式化时长
     */
    private String formatDuration(Integer seconds) {
        if (seconds == null) return null;
        int minutes = seconds / 60;
        if (minutes < 60) {
            return minutes + "分钟";
        } else {
            int hours = minutes / 60;
            int remainingMinutes = minutes % 60;
            return hours + "小时" + (remainingMinutes > 0 ? remainingMinutes + "分钟" : "");
        }
    }
}
