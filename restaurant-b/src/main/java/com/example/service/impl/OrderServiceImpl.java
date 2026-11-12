package com.example.service.impl;

import com.example.dto.*;
import com.example.entity.*;
import com.example.exception.ResourceNotFoundException;
import com.example.repository.*;
import com.example.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final TableRepository tableRepository;
    private final UserRepository userRepository;
    private final DishRepository dishRepository;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderDetailRepository orderDetailRepository, TableRepository tableRepository, UserRepository userRepository, DishRepository dishRepository) {
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.tableRepository = tableRepository;
        this.userRepository = userRepository;
        this.dishRepository = dishRepository;
    }

    @Override
    public List<OrderDTO> getAllOrdersWithDetails() {
        // 获取所有订单并按ID排序
        return orderRepository.findAllOrdersWithTableAndWaiter()
                .stream()
                .sorted(Comparator.comparing(OrderDTO::getOrderId))
                .collect(Collectors.toList());
    }

    // OrderServiceImpl.java
    @Override
    public List<OrderDTO> searchOrders(String keyword) {
        return orderRepository.searchOrdersNative(keyword).stream()
                .map(row -> {
                    OrderDTO dto = new OrderDTO();
                    dto.setOrderId((Integer) row[0]);
                    dto.setTableNumber((String) row[1]);
                    dto.setWaiterName((String) row[2]);
                    dto.setOrderTime((Timestamp) row[3]);
                    dto.setStatus((String) row[4]);
                    dto.setTotalAmount((BigDecimal) row[5]);

                    // 手动处理枚举转换
                    String paymentMethodStr = (String) row[6];
                    Order.PaymentMethod paymentMethod = null;
                    if (paymentMethodStr != null) {
                        try {
                            paymentMethod = Order.PaymentMethod.valueOf(paymentMethodStr);
                        } catch (IllegalArgumentException e) {
                            // 处理无效的支付方式
                        }
                    }
                    dto.setPaymentMethod(paymentMethod != null ? paymentMethod.name() : null);

                    dto.setPaymentTime((Timestamp) row[7]);
                    dto.setNotes((String) row[8]);
                    return dto;
                })
                .sorted(Comparator.comparing(OrderDTO::getOrderId))
                .collect(Collectors.toList());
    }

    @Override
    public OrderDTO getOrderById(Integer orderId) {
        return orderRepository.findOrderDTOById(orderId)
                .orElseThrow(() -> new RuntimeException("订单未找到"));
    }

    @Override
    @Transactional
    public void deleteOrder(Integer orderId) {
        // 先删除订单明细
        orderDetailRepository.deleteByOrder_OrderId(orderId);
        // 再删除订单
        orderRepository.deleteById(orderId);
    }



    @Override
    public List<OrderDetailDTO> getOrderDetails(Integer orderId) {
        List<OrderDetail> details = orderDetailRepository.findByOrderOrderId(orderId);
        return details.stream()
                .map(detail -> new OrderDetailDTO(
                        detail.getDetailId(),
                        detail.getDish().getDishName(), // 需要确保Dish实体有getName方法
                        detail.getQuantity(),
                        detail.getPrice()
                ))
                .collect(Collectors.toList());
    }
    // 添加获取单个订单明细的方法
    public OrderDetailDTO getOrderDetailById(Integer detailId) {
        OrderDetail detail = orderDetailRepository.findById(detailId)
                .orElseThrow(() -> new ResourceNotFoundException("订单明细不存在"));

        return new OrderDetailDTO(
                detail.getDetailId(),
                detail.getDish().getDishName(),
                detail.getQuantity(),
                detail.getPrice()
        );
    }
    @Override
    public OrderDTO createOrder(OrderCreateRequest request) {
        Order order = new Order();

        // 根据 tableId 获取餐桌实体
        TableEntity table = tableRepository.findById(request.getTableId())
                .orElseThrow(() -> new RuntimeException("餐桌不存在"));

        // 根据 waiterName 查找用户实体（服务员）
        User waiter = userRepository.findByFullName(request.getWaiterName())
                .orElseThrow(() -> new RuntimeException("服务员不存在"));

        // 设置关联
        order.setTable(table);
        order.setUser(waiter); // 关联用户ID

        // 设置其他属性
        order.setNotes(request.getNotes());
        order.setStatus("pending");
        order.setTotalAmount(BigDecimal.ZERO);
        order.setOrderTime(new Timestamp(System.currentTimeMillis()));

        if (StringUtils.hasText(request.getPaymentMethod())) {
            try {
                order.setPaymentMethod(Order.PaymentMethod.valueOf(request.getPaymentMethod()));
            } catch (IllegalArgumentException e) {
                // 处理无效支付方式
                order.setPaymentMethod(Order.PaymentMethod.other);
            }
        }

        Order savedOrder = orderRepository.save(order);
        updateTableStatus(table.getTableId(), TableEntity.TableStatus.occupied);
        return convertToDTO(savedOrder);
    }

    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setOrderId(order.getOrderId());
        dto.setTableNumber(order.getTable().getTableNumber());
        dto.setWaiterName(order.getUser().getFullName()); // 从User获取姓名
        dto.setOrderTime(order.getOrderTime());
        dto.setStatus(order.getStatus());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setPaymentMethod(order.getPaymentMethod() != null ?
                order.getPaymentMethod().name() : null);
        dto.setNotes(order.getNotes());
        return dto;
    }
    @Override
    @Transactional
    public OrderDetailDTO addOrderDetail(Integer orderId, AddOrderDetailRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("订单不存在"));

        Dish dish = dishRepository.findById(request.getDishId())
                .orElseThrow(() -> new ResourceNotFoundException("菜品不存在"));

        OrderDetail detail = new OrderDetail();
        detail.setOrder(order);
        detail.setDish(dish);
        detail.setQuantity(request.getQuantity());
        detail.setPrice(request.getPrice());


        OrderDetail savedDetail = orderDetailRepository.save(detail);
        recalculateOrderTotalAmount(orderId);
        return convertToDetailDTO(savedDetail);
    }

    // 新增：更新订单明细
    @Override
    @Transactional
    public OrderDetailDTO updateOrderDetail(Integer detailId, UpdateOrderDetailRequest request) {
        OrderDetail detail = orderDetailRepository.findById(detailId)
                .orElseThrow(() -> new ResourceNotFoundException("订单明细不存在"));

        if (request.getQuantity() != null) {
            detail.setQuantity(request.getQuantity());
        }
        if (request.getPrice() != null) {
            detail.setPrice(request.getPrice());
        }


        OrderDetail updatedDetail = orderDetailRepository.save(detail);
        recalculateOrderTotalAmount(detail.getOrder().getOrderId());
        return convertToDetailDTO(updatedDetail);
    }

    // 新增：删除订单明细
    @Override
    @Transactional
    public void deleteOrderDetail(Integer detailId) {
        OrderDetail detail = orderDetailRepository.findById(detailId)
                .orElseThrow(() -> new ResourceNotFoundException("订单明细不存在"));
        int orderId = detail.getOrder().getOrderId(); // 先获取订单ID
        orderDetailRepository.delete(detail);
        recalculateOrderTotalAmount(orderId); // 新增金额计算
    }

    // 辅助方法：转换OrderDetail为DTO
    private OrderDetailDTO convertToDetailDTO(OrderDetail detail) {
        return new OrderDetailDTO(
                detail.getDetailId(),
                detail.getDish().getDishName(),
                detail.getQuantity(),
                detail.getPrice()
        );
    }
    private void recalculateOrderTotalAmount(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("订单不存在"));

        // 获取订单所有明细项并计算总金额
        BigDecimal totalAmount = orderDetailRepository.findByOrderOrderId(orderId).stream()
                .map(detail -> detail.getPrice().multiply(BigDecimal.valueOf(detail.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 更新订单总金额
        order.setTotalAmount(totalAmount);
        orderRepository.save(order);
    }
    @Override
    @Transactional
    public OrderDTO updateOrder(Integer orderId, OrderUpdateRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("订单不存在"));

        // 记录旧状态用于状态变更检测
        String oldStatus = order.getStatus();
        String newStatus = request.getStatus();

        // 更新可修改字段
        if (newStatus != null) {
            order.setStatus(newStatus);
        }
        if (request.getPaymentMethod() != null) {
            try {
                order.setPaymentMethod(Order.PaymentMethod.valueOf(request.getPaymentMethod()));
            } catch (IllegalArgumentException e) {
                order.setPaymentMethod(Order.PaymentMethod.other);
            }
        }
        if (request.getNotes() != null) {
            order.setNotes(request.getNotes());
        }

        // 检查状态变更是否影响桌台占用状态
        checkTableStatusUpdate(order, oldStatus, newStatus);

        Order updatedOrder = orderRepository.save(order);
        return convertToDTO(updatedOrder);
    }

    private void checkTableStatusUpdate(Order order, String oldStatus, String newStatus) {
        // 状态未变化则无需处理
        if (newStatus == null || newStatus.equals(oldStatus)) {
            return;
        }

        TableEntity table = order.getTable();
        if (table == null) return;

        // 定义占用状态枚举
        final TableEntity.TableStatus OCCUPIED = TableEntity.TableStatus.occupied;
        final TableEntity.TableStatus AVAILABLE = TableEntity.TableStatus.available;

        // 处理状态变为已完成/已取消：释放桌台
        if (("completed".equals(newStatus) || "cancelled".equals(newStatus)) ){
            // 仅当桌台当前是占用状态时才更新
            if (OCCUPIED.equals(table.getStatus())) {
                updateTableStatus(table.getTableId(), AVAILABLE);
            }
        }
        // 处理从完成/取消状态恢复：重新占用桌台
    else if (("completed".equals(oldStatus) || "cancelled".equals(oldStatus))) {
            // 仅当桌台当前是可用状态时才更新
            if (AVAILABLE.equals(table.getStatus())) {
                updateTableStatus(table.getTableId(), OCCUPIED);
            }
        }
    }
    private void updateTableStatus(Integer tableId, TableEntity.TableStatus status) {
        TableEntity table = tableRepository.findById(tableId)
                .orElseThrow(() -> new ResourceNotFoundException("餐桌不存在"));
        table.setStatus(status);
        tableRepository.save(table);
    }

    // OrderServiceImpl.java 中修改 getKitchenOrders 方法
    @Override
    @Transactional(readOnly = true) // 添加事务注解避免懒加载问题
    public List<OrderKitchenDTO> getKitchenOrders() {
        // 获取状态为pending和preparing的订单
        List<Order> orders = orderRepository.findByStatusIn(List.of("pending", "preparing"));

        return orders.stream().map(order -> {
            OrderKitchenDTO dto = new OrderKitchenDTO(
                    order.getOrderId(),
                    order.getTable().getTableNumber(),
                    order.getStatus()
            );

            // 获取订单中的菜品（包含类别名称）
            List<KitchenItemDTO> items = order.getOrderDetails().stream()
                    .map(detail -> {
                        Dish dish = detail.getDish();
                        return new KitchenItemDTO(
                                dish.getDishId(),
                                dish.getDishName(),
                                detail.getQuantity(),
                                dish.getImageUrl(),
                                dish.getCategory().getCategoryName() // 获取类别名称
                        );
                    })
                    .collect(Collectors.toList());

            dto.setItems(items);
            return dto;
        }).collect(Collectors.toList());
    }

}
