package com.example.controller;

import com.example.dto.DishDTO;
import com.example.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dishes")
public class DishController {

    private final DishService dishService;

    @Autowired
    public DishController(DishService dishService) {
        this.dishService = dishService;
    }

    // 获取所有菜品
    @GetMapping
    public ResponseEntity<List<DishDTO>> getAllDishes() {
        List<DishDTO> dishes = dishService.getAllDishes();
        return ResponseEntity.ok(dishes);
    }

    // 模糊查询菜品
    @GetMapping("/search")
    public ResponseEntity<List<DishDTO>> searchDishes(@RequestParam String keyword) {
        List<DishDTO> dishes = dishService.searchDishesByName(keyword);
        return ResponseEntity.ok(dishes);
    }
    // 添加菜品
    @PostMapping
    public ResponseEntity<DishDTO> createDish(@RequestBody DishDTO dishDTO) {
        DishDTO createdDish = dishService.createDish(dishDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDish);
    }

    // 更新菜品
    @PutMapping("/{id}")
    public ResponseEntity<DishDTO> updateDish(@PathVariable int id, @RequestBody DishDTO dishDTO) {
        DishDTO updatedDish = dishService.updateDish(id, dishDTO);
        return ResponseEntity.ok(updatedDish);
    }

    // 删除菜品
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDish(@PathVariable int id) {
        dishService.deleteDish(id);
        return ResponseEntity.noContent().build();
    }
}