package com.example.repository;

import com.example.entity.Dish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DishRepository extends JpaRepository<Dish, Integer> {

    @Query("SELECT d FROM Dish d WHERE LOWER(d.dishName) LIKE LOWER(concat('%', :keyword, '%'))")
    List<Dish> findByDishNameContaining(@Param("keyword") String keyword);
}