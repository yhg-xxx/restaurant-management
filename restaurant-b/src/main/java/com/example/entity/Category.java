package com.example.entity;


import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private int categoryId;

    @Column(name = "category_name", unique = true, nullable = false, length = 50)
    private String categoryName;

    @Column(name = "description", length = 255)
    private String description;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Dish> dishes = new ArrayList<>();

    // 无参构造函数
    public Category() {
    }

    // 带参数的构造函数
    public Category(String categoryName, String description) {
        this.categoryName = categoryName;
        this.description = description;
    }

    // Getter和Setter方法
    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Dish> getDishes() {
        return dishes;
    }

    public void setDishes(List<Dish> dishes) {
        this.dishes = dishes;
    }

    // 辅助方法
    public void addDish(Dish dish) {
        dishes.add(dish);
        dish.setCategory(this);
    }

    public void removeDish(Dish dish) {
        dishes.remove(dish);
        dish.setCategory(null);
    }
}