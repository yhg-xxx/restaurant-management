package com.example.dto;

import java.util.List;

public class OrderKitchenDTO {
    private Integer orderId;
    private String tableNumber;
    private String status;
    private List<KitchenItemDTO> items;

    public OrderKitchenDTO() {}

    public OrderKitchenDTO(Integer orderId, String tableNumber, String status) {
        this.orderId = orderId;
        this.tableNumber = tableNumber;
        this.status = status;

    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<KitchenItemDTO> getItems() {
        return items;
    }

    public void setItems(List<KitchenItemDTO> items) {
        this.items = items;
    }


}