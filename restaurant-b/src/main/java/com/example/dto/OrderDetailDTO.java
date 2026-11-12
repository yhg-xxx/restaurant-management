package com.example.dto;

import java.math.BigDecimal;

public class OrderDetailDTO {
    private Integer detailId;
    private String dishName;
    private Integer quantity;
    private BigDecimal price;

    public OrderDetailDTO() {}

    public OrderDetailDTO(Integer detailId,String dishName, Integer quantity, BigDecimal price) {
        this.detailId = detailId;
        this.dishName = dishName;
        this.quantity = quantity;
        this.price = price;
    }
    public Integer getDetailId() { return detailId; }
    public void setDetailId(Integer detailId) { this.detailId = detailId; }


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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}