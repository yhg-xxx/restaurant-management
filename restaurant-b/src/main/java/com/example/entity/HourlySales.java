package com.example.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "hourly_sales")
public class HourlySales {
    @EmbeddedId
    private HourlySalesId id;  // 使用复合主键

    @Column(name = "order_count")
    private Integer orderCount;

    @Column(name = "total_sales", precision = 19, scale = 2)
    private BigDecimal totalSales;

    // 提供便捷方法访问主键字段
    public Double getHourOfDay() {
        return id != null ? id.getHourOfDay() : null;
    }

    public void setHourOfDay(Double hourOfDay) {
        if (id == null) id = new HourlySalesId();
        id.setHourOfDay(hourOfDay);
    }

    public String getDayOfWeek() {
        return id != null ? id.getDayOfWeek() : null;
    }

    public void setDayOfWeek(String dayOfWeek) {
        if (id == null) id = new HourlySalesId();
        id.setDayOfWeek(dayOfWeek);
    }

    // Getter和Setter
    public HourlySalesId getId() {
        return id;
    }

    public void setId(HourlySalesId id) {
        this.id = id;
    }

    public Integer getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(Integer orderCount) {
        this.orderCount = orderCount;
    }

    public BigDecimal getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(BigDecimal totalSales) {
        this.totalSales = totalSales;
    }
}