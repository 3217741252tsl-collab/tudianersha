package com.tudianersha.service;

import com.tudianersha.entity.ProjectHotel;
import com.tudianersha.entity.TravelProject;
import com.tudianersha.repository.ProjectHotelRepository;
import com.tudianersha.repository.TravelProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectHotelService {
    
    @Autowired
    private ProjectHotelRepository hotelRepository;
    
    @Autowired
    private TravelProjectRepository projectRepository;
    
    /**
     * 获取项目所有天的酒店信息
     */
    public List<ProjectHotel> getHotelsByProjectId(Long projectId) {
        return hotelRepository.findByProjectIdOrderByDayIndex(projectId);
    }
    
    /**
     * 获取某天的酒店信息
     */
    public Optional<ProjectHotel> getHotelByDay(Long projectId, Integer dayIndex) {
        return hotelRepository.findByProjectIdAndDayIndex(projectId, dayIndex);
    }
    
    /**
     * 设置酒店（如果是第一天且没有其他酒店记录，则自动复制到后续所有天）
     */
    @Transactional
    public ProjectHotel setHotel(Long projectId, Integer dayIndex, String hotelName, 
            String hotelAddress, BigDecimal longitude, BigDecimal latitude, BigDecimal pricePerNight) {
        
        // 获取项目天数
        TravelProject project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
        int totalDays = project.getDays();
        
        // 查看是否已有该天的酒店记录
        Optional<ProjectHotel> existingHotel = hotelRepository.findByProjectIdAndDayIndex(projectId, dayIndex);
        
        LocalDateTime now = LocalDateTime.now();
        ProjectHotel hotel;
        
        if (existingHotel.isPresent()) {
            // 更新现有记录
            hotel = existingHotel.get();
            hotel.setHotelName(hotelName);
            hotel.setHotelAddress(hotelAddress);
            hotel.setLongitude(longitude);
            hotel.setLatitude(latitude);
            hotel.setPricePerNight(pricePerNight);
            hotel.setUpdatedTime(now);
            hotel = hotelRepository.save(hotel);
        } else {
            // 创建新记录
            hotel = new ProjectHotel();
            hotel.setProjectId(projectId);
            hotel.setDayIndex(dayIndex);
            hotel.setHotelName(hotelName);
            hotel.setHotelAddress(hotelAddress);
            hotel.setLongitude(longitude);
            hotel.setLatitude(latitude);
            hotel.setPricePerNight(pricePerNight);
            hotel.setCreatedTime(now);
            hotel.setUpdatedTime(now);
            hotel = hotelRepository.save(hotel);
            
            // 如果是第一天，检查是否需要自动复制到后续天
            if (dayIndex == 1) {
                List<ProjectHotel> allHotels = hotelRepository.findByProjectIdOrderByDayIndex(projectId);
                // 如果只有刚创建的第一天记录，自动复制到后续所有天
                if (allHotels.size() == 1) {
                    for (int day = 2; day <= totalDays; day++) {
                        ProjectHotel copyHotel = new ProjectHotel();
                        copyHotel.setProjectId(projectId);
                        copyHotel.setDayIndex(day);
                        copyHotel.setHotelName(hotelName);
                        copyHotel.setHotelAddress(hotelAddress);
                        copyHotel.setLongitude(longitude);
                        copyHotel.setLatitude(latitude);
                        copyHotel.setPricePerNight(pricePerNight);
                        copyHotel.setCreatedTime(now);
                        copyHotel.setUpdatedTime(now);
                        hotelRepository.save(copyHotel);
                    }
                }
            }
        }
        
        return hotel;
    }
    
    /**
     * 更新某天的酒店信息
     */
    @Transactional
    public ProjectHotel updateHotel(Long hotelId, String hotelName, String hotelAddress,
            BigDecimal longitude, BigDecimal latitude, BigDecimal pricePerNight) {
        
        ProjectHotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new RuntimeException("酒店记录不存在"));
        
        hotel.setHotelName(hotelName);
        hotel.setHotelAddress(hotelAddress);
        hotel.setLongitude(longitude);
        hotel.setLatitude(latitude);
        hotel.setPricePerNight(pricePerNight);
        hotel.setUpdatedTime(LocalDateTime.now());
        
        return hotelRepository.save(hotel);
    }
    
    /**
     * 删除某天的酒店
     */
    @Transactional
    public void deleteHotel(Long hotelId) {
        hotelRepository.deleteById(hotelId);
    }
    
    /**
     * 删除项目所有酒店
     */
    @Transactional
    public void deleteAllByProjectId(Long projectId) {
        hotelRepository.deleteByProjectId(projectId);
    }
}
