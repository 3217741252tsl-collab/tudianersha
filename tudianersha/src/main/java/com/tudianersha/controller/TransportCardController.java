package com.tudianersha.controller;

import com.tudianersha.entity.TransportCard;
import com.tudianersha.entity.User;
import com.tudianersha.service.TransportCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transport-cards")
public class TransportCardController {
    
    @Autowired
    private TransportCardService transportCardService;
    
    /**
     * 一键生成交通路线
     */
    @PostMapping("/generate")
    public ResponseEntity<?> generateTransport(@RequestBody Map<String, Object> request, HttpSession session) {
        try {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                return ResponseEntity.status(401).body("未登录");
            }
            
            Long projectId = Long.valueOf(request.get("projectId").toString());
            Integer dayNumber = Integer.valueOf(request.get("dayNumber").toString());
            String fromActivityId = request.get("fromActivityId").toString();
            String toActivityId = request.get("toActivityId").toString();
            String fromName = request.get("fromName").toString();
            String toName = request.get("toName").toString();
            String fromLocation = request.get("fromLocation").toString();
            String toLocation = request.get("toLocation").toString();
            
            TransportCard card = transportCardService.generateTransport(
                projectId, dayNumber, fromActivityId, toActivityId,
                fromName, toName, fromLocation, toLocation, currentUser.getId()
            );
            
            return ResponseEntity.ok(card);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("生成失败: " + e.getMessage());
        }
    }
    
    /**
     * 手动编辑交通卡片
     */
    @PutMapping("/{cardId}/manual-edit")
    public ResponseEntity<?> manualEdit(@PathVariable Long cardId, 
                                       @RequestBody Map<String, Object> request,
                                       HttpSession session) {
        try {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                return ResponseEntity.status(401).body("未登录");
            }
            
            String method = request.get("method").toString();
            Integer duration = request.containsKey("duration") ? 
                Integer.valueOf(request.get("duration").toString()) : null;
            Double cost = request.containsKey("cost") ? 
                Double.valueOf(request.get("cost").toString()) : null;
            String customSteps = request.containsKey("customSteps") ? 
                request.get("customSteps").toString() : null;
            
            TransportCard card = transportCardService.manualEdit(
                cardId, method, duration, cost, customSteps, currentUser.getId()
            );
            
            return ResponseEntity.ok(card);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("编辑失败: " + e.getMessage());
        }
    }
    
    /**
     * 切换方案
     */
    @PutMapping("/{cardId}/switch-plan")
    public ResponseEntity<?> switchPlan(@PathVariable Long cardId,
                                       @RequestBody Map<String, Object> request,
                                       HttpSession session) {
        try {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                return ResponseEntity.status(401).body("未登录");
            }
            
            Integer planIndex = Integer.valueOf(request.get("planIndex").toString());
            
            TransportCard card = transportCardService.switchPlan(cardId, planIndex, currentUser.getId());
            
            return ResponseEntity.ok(card);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("切换失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取项目的所有交通卡片
     */
    @GetMapping("/project/{projectId}")
    public ResponseEntity<?> getProjectTransportCards(@PathVariable Long projectId, HttpSession session) {
        try {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                return ResponseEntity.status(401).body("未登录");
            }
            
            List<TransportCard> cards = transportCardService.getProjectTransportCards(projectId);
            
            return ResponseEntity.ok(cards);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("获取失败: " + e.getMessage());
        }
    }
}
