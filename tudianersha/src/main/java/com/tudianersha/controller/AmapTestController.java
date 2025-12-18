package com.tudianersha.controller;

import com.tudianersha.service.AmapPoiService;
import com.tudianersha.service.AmapDirectionService;
import com.tudianersha.service.KimiAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/amap-test")
public class AmapTestController {
    
    @Autowired
    private AmapPoiService amapPoiService;
    
    @Autowired
    private AmapDirectionService amapDirectionService;
    
    @Autowired
    private KimiAIService kimiAIService;
    
    /**
     * 获取两点之间的交通信息
     * GET /api/amap-test/direction?origin=116.481028,39.989643&destination=116.434446,39.90816&city=北京&destName=景点名称
     */
    @GetMapping("/direction")
    public ResponseEntity<Map<String, Object>> getDirection(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam(required = false, defaultValue = "北京") String city,
            @RequestParam(required = false) String destName) {
        
        System.out.println("[AmapTestController] /direction called - origin=" + origin + ", destination=" + destination + ", city=" + city + ", destName=" + destName);
        
        try {
            Map<String, Object> transportInfo = amapDirectionService.getTransportInfo(origin, destination, city, destName);
            return new ResponseEntity<>(transportInfo, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("[AmapTestController] Direction error: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("error", e.getMessage());
            return new ResponseEntity<>(errorData, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 测试获取景点图片
     * GET /api/amap-test/poi-photo?keyword=西湖&city=杭州
     */
    @GetMapping("/poi-photo")
    public ResponseEntity<Map<String, Object>> getPoiPhoto(
            @RequestParam String keyword,
            @RequestParam(required = false, defaultValue = "北京") String city) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 获取POI详细信息
            Map<String, Object> poiInfo = amapPoiService.searchPoiWithPhotos(keyword, city);
            
            // 获取第一张图片URL
            String photoUrl = amapPoiService.getPoiPhotoUrl(keyword, city);
            
            response.put("success", true);
            response.put("keyword", keyword);
            response.put("city", city);
            response.put("poiInfo", poiInfo);
            response.put("photoUrl", photoUrl);
            
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 获取POI详细信息（包括地址、开放时间等）
     * GET /api/amap-test/poi-detail?keyword=西湖&city=杭州
     */
    @GetMapping("/poi-detail")
    public ResponseEntity<Map<String, Object>> getPoiDetail(
            @RequestParam String keyword,
            @RequestParam String city) {
        
        System.out.println("[AmapTestController] /poi-detail called with keyword=" + keyword + ", city=" + city);
        
        try {
            // 先尝试直接搜索
            Map<String, Object> poiInfo = amapPoiService.searchPoiWithPhotos(keyword, city);
            
            // 如果直接搜索没结果，尝试提取景点名称
            if (poiInfo.isEmpty()) {
                String extractedName = extractAttractionName(keyword);
                if (!extractedName.equals(keyword)) {
                    System.out.println("[AmapTestController] Extracted attraction name: " + extractedName);
                    poiInfo = amapPoiService.searchPoiWithPhotos(extractedName, city);
                }
            }
            
            System.out.println("[AmapTestController] Returning data: " + poiInfo);
            
            // 直接返回结果，不使用模拟数据
            return new ResponseEntity<>(poiInfo, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("[AmapTestController] Error: " + e.getMessage());
            e.printStackTrace();
            
            // 返回错误信息而不是模拟数据
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("error", e.getMessage());
            errorData.put("keyword", keyword);
            errorData.put("city", city);
            return new ResponseEntity<>(errorData, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 从复杂的活动描述中提取景点名称
     * 例："步行至'东莞可园'参观岭南园林建筑" -> "东莞可园"
     */
    private String extractAttractionName(String description) {
        // 匹配单引号中的内容
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("'([^']+)'");
        java.util.regex.Matcher matcher = pattern.matcher(description);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        // 匹配双引号中的内容
        pattern = java.util.regex.Pattern.compile("\"([^\"]+)\"");
        matcher = pattern.matcher(description);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        // 匹配中文引号中的内容
        pattern = java.util.regex.Pattern.compile("「([^」]+)」");
        matcher = pattern.matcher(description);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        // 移除常见的动作词
        String cleaned = description
            .replaceAll("^（?步行至｜乘坐.*?前往｜参观｜游览｜前往｜在.*?享用）?", "")
            .replaceAll("（参观.*｜游览.*｜享用.*）$", "")
            .trim();
        
        if (!cleaned.isEmpty() && !cleaned.equals(description)) {
            return cleaned;
        }
        
        return description;
    }
    
    /**
     * 使用Kimi AI生成景点介绍
     * GET /api/amap-test/attraction-intro?name=西湖&city=杭州
     */
    @GetMapping("/attraction-intro")
    public ResponseEntity<Map<String, Object>> getAttractionIntro(
            @RequestParam String name,
            @RequestParam String city) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String prompt = String.format(
                "你是一个专业的旅游咨询顾问，请查找%s%s的真实信息，并严格按下面格式返回：\n\n" +
                "1. 【开放时间】：具体的开放时间（例如：08:00-18:00，全年全天等，如果真的无法查询到准确时间才写'请查看官方公告'）\n" +
                "2. 【门票信息】：门票价格（例如：成人票XX元、学生票XX元、免费等，如果真的无法查询到准确价格才写'请查看官方公告'）、是否需要预约\n" +
                "3. 【游览建议】：简要的游览贴士和建议（约50字）\n\n" +
                "请直接返回以上三项内容，不需要其他开场白和结束语。注意：开放时间和门票信息必须提供具体数据，不能仅用'请查看官方公告'或'建议提前了解'等模糊表述来回答。",
                city, name
            );
            
            String introduction = kimiAIService.generateRoute(prompt);
            
            response.put("success", true);
            response.put("introduction", introduction);
            
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 获取附近景点推荐（使用高德周边搜索API）
     * GET /api/amap-test/nearby-attractions?name=西湖&city=杭州
     */
    @GetMapping("/nearby-attractions")
    public ResponseEntity<Map<String, Object>> getNearbyAttractions(
            @RequestParam String name,
            @RequestParam String city) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> mainPoi = amapPoiService.searchPoiWithPhotos(name, city);
            
            if (mainPoi == null || !mainPoi.containsKey("location")) {
                System.out.println("[AmapTestController] No location found for attraction");
                response.put("success", false);
                response.put("error", "No location found");
                response.put("attractions", new ArrayList<>());
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
            
            String location = (String) mainPoi.get("location");
            List<Map<String, Object>> attractions = amapPoiService.searchNearbyAttractions(location, 3000);
            
            attractions.removeIf(a -> name.equals(a.get("name")));
            
            response.put("success", true);
            response.put("attractions", attractions);
            
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (Exception e) {
            System.err.println("[AmapTestController] Nearby error: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("attractions", new ArrayList<>());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 地理编码：将地址转换为城市名称
     * GET /api/amap-test/geocode?address=杭州西湖
     */
    @GetMapping("/geocode")
    public ResponseEntity<Map<String, Object>> geocode(@RequestParam String address) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 使用高德POI搜索获取城市信息
            Map<String, Object> poiInfo = amapPoiService.searchPoiWithPhotos(address, "");
            
            String city = null;
            
            // 尝试从地址中提取城市名
            if (poiInfo.containsKey("address")) {
                String fullAddress = (String) poiInfo.get("address");
                // 简单提取逻辑：取第一个市级单位
                if (fullAddress.contains("市")) {
                    int idx = fullAddress.indexOf("市");
                    String before = fullAddress.substring(0, idx);
                    int start = Math.max(0, before.lastIndexOf("省") + 1);
                    city = before.substring(start) + "市";
                }
            }
            
            // 如果没有提取到，直接使用address作为城市
            if (city == null || city.isEmpty()) {
                city = address.replaceAll("[省市区县]", "");
            }
            
            response.put("success", true);
            response.put("city", city);
            response.put("address", address);
            
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (Exception e) {
            System.err.println("[AmapTestController] Geocode error: " + e.getMessage());
            // 即使出错也返回基本信息
            response.put("success", true);
            response.put("city", address.replaceAll("[省市区县]", ""));
            response.put("address", address);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }
    
    /**
     * POI搜索：根据关键词和类型搜索景点
     * GET /api/amap-test/poi-search?keyword=公园&city=杭州&types=110000
     */
    @GetMapping("/poi-search")
    public ResponseEntity<Map<String, Object>> searchPoi(
            @RequestParam String keyword,
            @RequestParam String city,
            @RequestParam(required = false) String types) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 使用高德POI搜索
            List<Map<String, Object>> pois = amapPoiService.searchPoiList(keyword, city, types);
            
            response.put("success", true);
            response.put("pois", pois);
            response.put("count", pois.size());
            
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (Exception e) {
            System.err.println("[AmapTestController] POI search error: " + e.getMessage());
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("pois", new ArrayList<>());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }
}
