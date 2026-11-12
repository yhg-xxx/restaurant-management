package com.example.dto;

import com.example.entity.Order;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class OrderDTO {
    private Integer orderId;
    private String tableNumber;
    private String waiterName;
    private Timestamp orderTime;
    private String status;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private Timestamp paymentTime;
    private String notes;

    public OrderDTO(Integer orderId, String tableNumber, String waiterName,
                    Timestamp orderTime, String status,
                    BigDecimal totalAmount, Order.PaymentMethod paymentMethod,
                    Timestamp paymentTime, String notes) {
        this.orderId = orderId;
        this.tableNumber = tableNumber;
        this.waiterName = waiterName;
        this.orderTime = orderTime;
        this.status = status ;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod != null ? paymentMethod.name() : null;
        this.paymentTime = paymentTime;
        this.notes = notes;
    }

    // 添加无参构造器
    public OrderDTO() {}


    // 省略所有 Getter 和 Setter 方法（实际代码中需完整实现）
    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    public String getTableNumber() { return tableNumber; }
    public void setTableNumber(String tableNumber) { this.tableNumber = tableNumber; }
    public String getWaiterName() { return waiterName; }
    public void setWaiterName(String waiterName) { this.waiterName = waiterName; }
    public Timestamp getOrderTime() { return orderTime; }
    public void setOrderTime(Timestamp orderTime) { this.orderTime = orderTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public Timestamp getPaymentTime() { return paymentTime; }
    public void setPaymentTime(Timestamp paymentTime) { this.paymentTime = paymentTime; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}