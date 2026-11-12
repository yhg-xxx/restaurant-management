package com.example.dto;


import java.math.BigDecimal;

public class CategorySalesDTO {
    private String category;
    private BigDecimal  sales;

    public CategorySalesDTO(String category, BigDecimal sales) {
        this.category = category;
        this.sales = sales;
    }

    public CategorySalesDTO() {

    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getSales() {
        return sales;
    }

    public void setSales(BigDecimal sales) {
        this.sales = sales;
    }
}