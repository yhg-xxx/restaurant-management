package com.example.controller;

import com.example.dto.*;
import com.example.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // 获取所有订单
    @GetMapping
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        List<OrderDTO> orders = orderService.getAllOrdersWithDetails();
        return ResponseEntity.ok(orders);
    }

    // 搜索订单
    @GetMapping("/search")
    public ResponseEntity<List<OrderDTO>> searchOrders(@RequestParam String keyword) {
        List<OrderDTO> orders = orderService.searchOrders(keyword);
        return ResponseEntity.ok(orders);
    }

    // 获取订单详情
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Integer orderId) {
        OrderDTO order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(order);
    }

    // 获取订单明细（菜品）
    @GetMapping("/{orderId}/items")
    public ResponseEntity<List<OrderDetailDTO>> getOrderDetails(@PathVariable Integer orderId) {
        List<OrderDetailDTO> items = orderService.getOrderDetails(orderId);
        return ResponseEntity.ok(items);
    }
    // 添加获取单个订单明细的端点
    @GetMapping("/items/{detailId}")
    public ResponseEntity<OrderDetailDTO> getOrderDetailById(@PathVariable Integer detailId) {
        OrderDetailDTO detail = orderService.getOrderDetailById(detailId);
        return ResponseEntity.ok(detail);
    }

    // 删除订单
    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Integer orderId) {
        orderService.deleteOrder(orderId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@RequestBody OrderCreateRequest request) {
        OrderDTO createdOrder = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    @PostMapping("/{orderId}/items")
    public ResponseEntity<OrderDetailDTO> addOrderDetail(
            @PathVariable Integer orderId,
            @RequestBody AddOrderDetailRequest request) {
        OrderDetailDTO detail = orderService.addOrderDetail(orderId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(detail);
    }

    //更新订单明细
    @PutMapping("/items/{detailId}")
    public ResponseEntity<OrderDetailDTO> updateOrderDetail(
            @PathVariable Integer detailId,
            @RequestBody UpdateOrderDetailRequest request) {
        OrderDetailDTO updatedDetail = orderService.updateOrderDetail(detailId, request);
        return ResponseEntity.ok(updatedDetail);
    }

    //删除订单明细
    @DeleteMapping("/items/{detailId}")
    public ResponseEntity<Void> deleteOrderDetail(@PathVariable Integer detailId) {
        orderService.deleteOrderDetail(detailId);
        return ResponseEntity.noContent().build();
    }
    // 添加更新订单状态的接口
    @PutMapping("/{orderId}")
    public ResponseEntity<OrderDTO> updateOrder(
            @PathVariable Integer orderId,
            @RequestBody OrderUpdateRequest request) {

        OrderDTO updatedOrder = orderService.updateOrder(orderId, request);
        return ResponseEntity.ok(updatedOrder);
    }

    @GetMapping("/kitchen/orders")
    public ResponseEntity<List<OrderKitchenDTO>> getKitchenOrders() {
        List<OrderKitchenDTO> orders = orderService.getKitchenOrders();
        return ResponseEntity.ok(orders);
    }
}