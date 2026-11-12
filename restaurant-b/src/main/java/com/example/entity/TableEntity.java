package com.example.entity;



import jakarta.persistence.*;

@Entity
@Table(name = "tables")
public class TableEntity {

    public enum TableStatus {
        available,   // 可用（空闲状态）
        occupied,    // 已占用（有客人正在用餐）
        reserved     // 已预订（未来某个时间被预定）
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "table_id")
    private Integer tableId;

    @Column(name = "table_number", unique = true, nullable = false, length = 20)
    private String tableNumber;

    @Column(nullable = false)
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    private TableStatus status = TableStatus.available;

    @Column(length = 100)
    private String location;

    public TableEntity() {
    }

    public TableEntity(String tableNumber, Integer capacity, TableStatus status, String location) {
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.status = status;
        this.location = location;
    }

    // Getters and Setters
    public Integer getTableId() {
        return tableId;
    }

    public void setTableId(Integer tableId) {
        this.tableId = tableId;
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public TableStatus getStatus() {
        return status;
    }

    public void setStatus(TableStatus status) {
        this.status = status;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}