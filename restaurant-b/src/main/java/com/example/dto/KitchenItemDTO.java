package com.example.dto;

public class KitchenItemDTO {
    private Integer dishId;
    private String dishName;
    private Integer quantity;
    private String imageUrl;
    private String categoryName;


    public KitchenItemDTO() {}

    public KitchenItemDTO(Integer dishId, String dishName, Integer quantity, String imageUrl,String categoryName) {
        this.dishId = dishId;
        this.dishName = dishName;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
        this.categoryName = categoryName;
    }

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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}