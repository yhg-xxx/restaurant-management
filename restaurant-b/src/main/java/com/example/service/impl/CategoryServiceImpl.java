package com.example.service.impl;

import com.example.dto.CategoryDTO;
import com.example.entity.Category;
import com.example.repository.CategoryRepository;
import com.example.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<CategoryDTO> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream().map(category -> {
            CategoryDTO dto = new CategoryDTO();
            BeanUtils.copyProperties(category, dto);
            return dto;
        }).collect(Collectors.toList());
    }
}