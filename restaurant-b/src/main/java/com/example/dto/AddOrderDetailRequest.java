package com.example.dto;

import java.math.BigDecimal;

public class AddOrderDetailRequest {
    private Integer dishId;
    private Integer quantity;
    private BigDecimal price;


    public Integer getDishId() { return dishId; }
    public void setDishId(Integer dishId) { this.dishId = dishId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

}