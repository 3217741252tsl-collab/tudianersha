package com.tudianersha.repository;

import com.tudianersha.entity.TransportCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransportCardRepository extends JpaRepository<TransportCard, Long> {
    
    /**
     * 根据项目ID查询所有交通卡片
     */
    List<TransportCard> findByProjectIdOrderByDayNumberAsc(Long projectId);
    
    /**
     * 根据项目ID和天数查询交通卡片
     */
    List<TransportCard> findByProjectIdAndDayNumber(Long projectId, Integer dayNumber);
    
    /**
     * 根据起点和终点活动ID查询交通卡片
     */
    TransportCard findByProjectIdAndFromActivityIdAndToActivityId(Long projectId, String fromActivityId, String toActivityId);
    
    /**
     * 删除项目的所有交通卡片
     */
    void deleteByProjectId(Long projectId);
}
