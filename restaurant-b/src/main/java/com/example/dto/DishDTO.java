package com.example.dto;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class DishDTO {
    private int dishId;
    private String dishName;
    private String categoryName; // 类别名称而非ID
    private BigDecimal price;
    private String description;
    @NotBlank(message = "图片URL不能为空")
    private String imageUrl;

    private boolean available;
    private Timestamp createdAt;

    public DishDTO() {}

    public int getDishId() { return dishId; }
    public void setDishId(int dishId) { this.dishId = dishId; }
    public String getDishName() { return dishName; }
    public void setDishName(String dishName) { this.dishName = dishName; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}