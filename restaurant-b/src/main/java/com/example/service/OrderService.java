package com.example.service;

import com.example.dto.*;


import java.util.List;

public interface OrderService {
    List<OrderDTO> getAllOrdersWithDetails(); // 改为返回列表
    OrderDTO getOrderById(Integer orderId);
    void deleteOrder(Integer orderId);
    List<OrderDTO> searchOrders(String keyword); // 改为返回列表
    List<OrderDetailDTO> getOrderDetails(Integer orderId);
    OrderDTO createOrder(OrderCreateRequest request);
    OrderDetailDTO addOrderDetail(Integer orderId, AddOrderDetailRequest request);
    OrderDetailDTO updateOrderDetail(Integer detailId, UpdateOrderDetailRequest request);
    void deleteOrderDetail(Integer detailId);
    OrderDetailDTO getOrderDetailById(Integer detailId);
    OrderDTO updateOrder(Integer orderId, OrderUpdateRequest request);
    List<OrderKitchenDTO> getKitchenOrders();
}