package com.example.service;

import com.example.dto.DishDTO;

import java.util.List;

public interface DishService {
    List<DishDTO> getAllDishes();
    List<DishDTO> searchDishesByName(String keyword);
    DishDTO createDish(DishDTO dishDTO);
    DishDTO updateDish(int id, DishDTO dishDTO);
    void deleteDish(int id);
}