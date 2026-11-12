package com.example.service.impl;

import com.example.dto.DishDTO;
import com.example.entity.Category;
import com.example.entity.Dish;
import com.example.exception.ResourceNotFoundException;
import com.example.repository.CategoryRepository;
import com.example.repository.DishRepository;
import com.example.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl implements DishService {

    private final DishRepository dishRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public DishServiceImpl(DishRepository dishRepository, CategoryRepository categoryRepository) {
        this.dishRepository = dishRepository;
        this.categoryRepository = categoryRepository;
    }

    private DishDTO convertToDTO(Dish dish) {
        DishDTO dto = new DishDTO();
        BeanUtils.copyProperties(dish, dto);
        dto.setCategoryName(dish.getCategory().getCategoryName()); // 设置类别名称

        // 特别注意：实体中属性名为 isAvailable，DTO 中为 available
        dto.setAvailable(dish.isAvailable());

        return dto;
    }

    // 将DishDTO转换为Dish实体
    private Dish convertToEntity(DishDTO dto) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dto, dish, "categoryName", "dishId");

        // 根据类别名称查找类别
        Category category = categoryRepository.findByCategoryName(dto.getCategoryName());
        if (category == null) {
            throw new ResourceNotFoundException("Category not found with name: " + dto.getCategoryName());
        }
        dish.setCategory(category);

        return dish;
    }

    @Override
    public List<DishDTO> getAllDishes() {
        List<Dish> dishList = dishRepository.findAll();
        return dishList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<DishDTO> searchDishesByName(String keyword) {
        // 将查询关键字转换为小写
        String lowerKeyword = keyword.toLowerCase();

        List<Dish> dishList = dishRepository.findAll().stream()
                .filter(dish -> dish.getDishName().toLowerCase().contains(lowerKeyword))
                .toList();

        return dishList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DishDTO createDish(DishDTO dishDTO) {
        Dish dish = convertToEntity(dishDTO);
        dish.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        Dish savedDish = dishRepository.save(dish);
        return convertToDTO(savedDish);
    }

    @Override
    @Transactional
    public DishDTO updateDish(int id, DishDTO dishDTO) {
        Dish existingDish = dishRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dish not found with id: " + id));

        // 更新字段（排除ID和创建时间）
        existingDish.setDishName(dishDTO.getDishName());
        existingDish.setPrice(dishDTO.getPrice());
        existingDish.setDescription(dishDTO.getDescription());
        existingDish.setImageUrl(dishDTO.getImageUrl());
        existingDish.setAvailable(dishDTO.isAvailable());

        // 更新类别（如果需要）
        if (!existingDish.getCategory().getCategoryName().equals(dishDTO.getCategoryName())) {
            Category category = categoryRepository.findByCategoryName(dishDTO.getCategoryName());
            if (category == null) {
                throw new ResourceNotFoundException("Category not found with name: " + dishDTO.getCategoryName());
            }
            existingDish.setCategory(category);
        }

        Dish updatedDish = dishRepository.save(existingDish);
        return convertToDTO(updatedDish);
    }

    @Override
    @Transactional
    public void deleteDish(int id) {
        Dish dish = dishRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dish not found with id: " + id));
        dishRepository.delete(dish);
    }
}