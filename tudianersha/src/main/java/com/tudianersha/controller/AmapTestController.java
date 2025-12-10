package com.tudianersha.controller;

import com.tudianersha.service.AmapPoiService;
import com.tudianersha.service.KimiAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/amap-test")
public class AmapTestController {
    
    @Autowired
    private AmapPoiService amapPoiService;
    
    @Autowired
    private KimiAIService kimiAIService;
    
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
     * 获取景点详细信息（包括多张图片、地址等）
     * GET /api/amap-test/poi-detail?keyword=西湖&city=杭州
     */
    @GetMapping("/poi-detail")
    public ResponseEntity<Map<String, Object>> getPoiDetail(
            @RequestParam String keyword,
            @RequestParam(required = false, defaultValue = "北京") String city) {
        
        try {
            System.out.println("[Controller] Getting POI detail for: " + keyword + " in " + city);
            
            // 获取POI详细信息
            Map<String, Object> poiInfo = amapPoiService.searchPoiWithPhotos(keyword, city);
            
            System.out.println("[Controller] POI Info result: " + (poiInfo != null ? poiInfo.keySet() : "null"));
            
            if (poiInfo == null || poiInfo.isEmpty()) {
                System.out.println("[Controller] POI info is null or empty");
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "未找到景点信息");
                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
            }
            
            System.out.println("[Controller] Returning OK response with " + poiInfo.size() + " fields");
            return new ResponseEntity<>(poiInfo, HttpStatus.OK);
            
        } catch (Exception e) {
            System.err.println("[Controller] Error in getPoiDetail: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 生成景点AI介绍
     * GET /api/amap-test/attraction-intro?name=西湖&city=杭州
     */
    @GetMapping("/attraction-intro")
    public ResponseEntity<Map<String, Object>> getAttractionIntroduction(
            @RequestParam String name,
            @RequestParam(required = false, defaultValue = "北京") String city) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 调用Kimi AI生成介绍
            String introduction = kimiAIService.generateAttractionIntroduction(name, city);
            
            response.put("success", true);
            response.put("name", name);
            response.put("city", city);
            response.put("introduction", introduction);
            
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 搜索POI（用于自动补全）
     * GET /api/amap-test/poi-search?keyword=西&city=杭州&types=110000|120000|130000|140000
     */
    @GetMapping("/poi-search")
    public ResponseEntity<Map<String, Object>> searchPoi(
            @RequestParam String keyword,
            @RequestParam String city,
            @RequestParam(required = false, defaultValue = "110000|120000|130000|140000") String types) {
        
        try {
            // 调用服务层搜索POI
            Map<String, Object> result = amapPoiService.searchPoi(keyword, city, types);
            
            if (result == null || result.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "未找到相关景点");
                errorResponse.put("pois", new Object[0]);
                return new ResponseEntity<>(errorResponse, HttpStatus.OK);
            }
            
            return new ResponseEntity<>(result, HttpStatus.OK);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("pois", new Object[0]);
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 地理编码（将位置名称转换为城市名称）
     * GET /api/amap-test/geocode?address=虚门站
     */
    @GetMapping("/geocode")
    public ResponseEntity<Map<String, Object>> geocode(@RequestParam String address) {
        try {
            Map<String, Object> result = amapPoiService.geocode(address);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
