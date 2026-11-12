package com.example.repository;

import com.example.entity.HourlySales;
import com.example.entity.HourlySalesId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface HourlySalesRepository extends JpaRepository<HourlySales, HourlySalesId> {
    // 使用JPQL确保正确排序
    @Query("SELECT hs FROM HourlySales hs ORDER BY hs.id.dayOfWeek ASC, hs.id.hourOfDay ASC")
    List<HourlySales> findAllByOrderByDayOfWeekAscHourOfDayAsc();
}