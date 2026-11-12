package com.example.repository;

import com.example.dto.OrderDTO;
import com.example.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    // 获取所有订单
    @Query("SELECT new com.example.dto.OrderDTO(" +
            "o.orderId, t.tableNumber, u.fullName, o.orderTime, " +
            "o.status, o.totalAmount, o.paymentMethod, o.paymentTime, o.notes) " +
            "FROM Order o " +
            "JOIN o.table t " +
            "JOIN o.user u " +
            "ORDER BY o.orderId ASC")
    List<OrderDTO> findAllOrdersWithTableAndWaiter();

    @Query(value = "SELECT o.order_id, t.table_number, " +
            "u.full_name, o.order_time, " +
            "o.status, o.total_amount, " +
            "o.payment_method, o.payment_time, o.notes " +
            "FROM orders o " +
            "JOIN tables t ON o.table_id = t.table_id " +
            "JOIN users u ON o.user_id = u.user_id " +
            "WHERE " +
            "   CAST(o.order_id AS TEXT) ILIKE '%' || :keyword || '%' " +
            "   OR t.table_number ILIKE '%' || :keyword || '%' " +
            "   OR u.full_name ILIKE '%' || :keyword || '%' " +
            "ORDER BY o.order_id ASC",
            nativeQuery = true)
    List<Object[]> searchOrdersNative(@Param("keyword") String keyword);

    // 新增查询方法
    @Query("SELECT new com.example.dto.OrderDTO(" +
            "o.orderId, t.tableNumber, u.fullName, o.orderTime, " +
            "o.status, o.totalAmount, o.paymentMethod, o.paymentTime, o.notes) " +
            "FROM Order o " +
            "JOIN o.table t " +
            "JOIN o.user u " +
            "WHERE o.orderId = :orderId")
    Optional<OrderDTO> findOrderDTOById(Integer orderId);

    List<Order> findByStatusIn(List<String> statuses);
}