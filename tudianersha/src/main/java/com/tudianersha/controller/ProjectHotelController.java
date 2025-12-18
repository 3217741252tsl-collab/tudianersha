package com.tudianersha.controller;

import com.tudianersha.dto.ApiResponse;
import com.tudianersha.entity.ProjectHotel;
import com.tudianersha.service.ProjectHotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/project-hotels")
public class ProjectHotelController {
    
    @Autowired
    private ProjectHotelService hotelService;
    
    /**
     * 获取项目所有天的酒店信息
     */
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<ProjectHotel>> getHotelsByProject(@PathVariable Long projectId) {
        List<ProjectHotel> hotels = hotelService.getHotelsByProjectId(projectId);
        return ResponseEntity.ok(hotels);
    }
    
    /**
     * 获取某天的酒店信息
     */
    @GetMapping("/project/{projectId}/day/{dayIndex}")
    public ResponseEntity<ProjectHotel> getHotelByDay(
            @PathVariable Long projectId, 
            @PathVariable Integer dayIndex) {
        return hotelService.getHotelByDay(projectId, dayIndex)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 设置酒店（第一天会自动复制到后续天）
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ProjectHotel>> setHotel(@RequestBody Map<String, Object> request) {
        try {
            Long projectId = Long.valueOf(request.get("projectId").toString());
            Integer dayIndex = Integer.valueOf(request.get("dayIndex").toString());
            String hotelName = (String) request.get("hotelName");
            String hotelAddress = (String) request.get("hotelAddress");
            BigDecimal longitude = new BigDecimal(request.get("longitude").toString());
            BigDecimal latitude = new BigDecimal(request.get("latitude").toString());
            BigDecimal pricePerNight = new BigDecimal(request.get("pricePerNight").toString());
            
            ProjectHotel hotel = hotelService.setHotel(projectId, dayIndex, hotelName, 
                    hotelAddress, longitude, latitude, pricePerNight);
            
            return ResponseEntity.ok(ApiResponse.success("酒店设置成功", hotel));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("酒店设置失败: " + e.getMessage()));
        }
    }
    
    /**
     * 更新某天的酒店
     */
    @PutMapping("/{hotelId}")
    public ResponseEntity<ApiResponse<ProjectHotel>> updateHotel(
            @PathVariable Long hotelId,
            @RequestBody Map<String, Object> request) {
        try {
            String hotelName = (String) request.get("hotelName");
            String hotelAddress = (String) request.get("hotelAddress");
            BigDecimal longitude = new BigDecimal(request.get("longitude").toString());
            BigDecimal latitude = new BigDecimal(request.get("latitude").toString());
            BigDecimal pricePerNight = new BigDecimal(request.get("pricePerNight").toString());
            
            ProjectHotel hotel = hotelService.updateHotel(hotelId, hotelName, 
                    hotelAddress, longitude, latitude, pricePerNight);
            
            return ResponseEntity.ok(ApiResponse.success("酒店更新成功", hotel));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("酒店更新失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除某天的酒店
     */
    @DeleteMapping("/{hotelId}")
    public ResponseEntity<ApiResponse<Void>> deleteHotel(@PathVariable Long hotelId) {
        try {
            hotelService.deleteHotel(hotelId);
            return ResponseEntity.ok(ApiResponse.success("酒店删除成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("酒店删除失败: " + e.getMessage()));
        }
    }
}
