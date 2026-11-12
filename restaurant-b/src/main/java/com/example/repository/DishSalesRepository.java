// DishSalesRepository.java
package com.example.repository;

import com.example.entity.DishSales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DishSalesRepository extends JpaRepository<DishSales, Integer> {

    @Query(value = "SELECT * FROM dish_sales ORDER BY total_sales DESC LIMIT 10", nativeQuery = true)
    List<DishSales> findTop10Dishes();
}