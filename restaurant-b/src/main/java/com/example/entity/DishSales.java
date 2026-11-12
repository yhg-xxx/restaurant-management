package com.example.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "dish_sales")
public class DishSales {
    @Id
    @Column(name = "dish_id")
    private Integer dishId;

    @Column(name = "dish_name")
    private String dishName;

    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "total_quantity")
    private Integer totalQuantity;

    @Column(name = "total_sales", precision = 19, scale = 2)
    private BigDecimal totalSales;

    // 移除 getId() 和 setId() 方法


    public Integer getDishId() {
        return dishId;
    }

    public void setDishId(Integer dishId) {
        this.dishId = dishId;
    }

    public String getDishName() {
        return dishName;
    }

    public void setDishName(String dishName) {
        this.dishName = dishName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public BigDecimal getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(BigDecimal totalSales) {
        this.totalSales = totalSales;
    }
}