package com.example.dto;


import java.math.BigDecimal;

public class DishRankDTO {
    private int dishId;
    private String dishName;
    private BigDecimal totalSales;

    public DishRankDTO(int dishId, String dishName, BigDecimal totalSales) {
        this.dishId = dishId;
        this.dishName = dishName;
        this.totalSales = totalSales;
    }

    public DishRankDTO() {

    }

    public int getDishId() {
        return dishId;
    }

    public void setDishId(int dishId) {
        this.dishId = dishId;
    }

    public String getDishName() {
        return dishName;
    }

    public void setDishName(String dishName) {
        this.dishName = dishName;
    }

    public BigDecimal getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(BigDecimal totalSales) {
        this.totalSales = totalSales;
    }
}