package com.example.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Integer orderId;



    @Column(name = "order_time", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp orderTime;

    @Column(length = 20)
    @Pattern(regexp = "pending|confirmed|preparing|ready|served|completed|cancelled", message = "Invalid status")
    private String status; // 字符串类型

    @Column(name = "total_amount", precision = 10, scale = 2, columnDefinition = "DECIMAL(10,2) DEFAULT '0.00'")
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", columnDefinition = "VARCHAR(10) CHECK (payment_method IN ('cash','alipay','wechat','card','other'))")
    private PaymentMethod paymentMethod;

    @Column(name = "payment_time")
    private Timestamp paymentTime;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> orderDetails;
    @ManyToOne
    @JoinColumn(name = "table_id", referencedColumnName = "table_id", nullable = false)
    private TableEntity table;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
    private User user;

    // 修改 Getter 和 Setter
    public TableEntity getTable() {
        return table;
    }

    public void setTable(TableEntity table) {
        this.table = table;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // 枚举定义
    public enum PaymentMethod {
        cash, alipay, wechat, card, other
    }

    // Getters and Setters
    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }



    public Timestamp getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(Timestamp orderTime) {
        this.orderTime = orderTime;
    }

    public @Pattern(regexp = "pending|confirmed|preparing|ready|served|completed|cancelled", message = "Invalid status") String getStatus() {
        return status;
    }

    public void setStatus(@Pattern(regexp = "pending|confirmed|preparing|ready|served|completed|cancelled", message = "Invalid status") String status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Timestamp getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(Timestamp paymentTime) {
        this.paymentTime = paymentTime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<OrderDetail> getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(List<OrderDetail> orderDetails) {
        this.orderDetails = orderDetails;
    }
}