package com.bookbrew.order.service.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bookbrew.order.service.dto.OrderItemRequestDTO;
import com.bookbrew.order.service.dto.OrderRequestDTO;
import com.bookbrew.order.service.exception.BusinessException;
import com.bookbrew.order.service.exception.ResourceNotFoundException;
import com.bookbrew.order.service.model.Order;
import com.bookbrew.order.service.model.OrderItems;
import com.bookbrew.order.service.model.Payment;
import com.bookbrew.order.service.repository.OrderItemsRepository;
import com.bookbrew.order.service.repository.OrderRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemsRepository orderItemsRepository;

    public Order createOrder(OrderRequestDTO orderRequest) {
        Order order = new Order();
        order.setCustomerDTO(orderRequest.getCustomer());
        order.setCreationDate(LocalDateTime.now());
        order.setStatus("Pedido em análise");

        Order savedOrder = orderRepository.save(order);

        List<OrderItems> orderItems = orderRequest.getItems().stream()
                .map(item -> createOrderItem(item, savedOrder))
                .collect(Collectors.toList());

        savedOrder.setOrderItems(orderItems);

        // Calculate total amount and process payment here
        processPayment(savedOrder, orderRequest.getPayment());

        return savedOrder;
    }

    private OrderItems createOrderItem(OrderItemRequestDTO itemRequest, Order order) {
        OrderItems orderItem = new OrderItems();
        orderItem.setProductId(itemRequest.getProductId());
        orderItem.setQuantity(itemRequest.getQuantity());
        orderItem.setPrice(itemRequest.getPrice());
        orderItem.setOrderId(order);
        return orderItemsRepository.save(orderItem);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    private void processPayment(Order order, Payment payment) {
        // Implement payment processing logic here
        // Update order status based on payment result
    }

    public Order updateOrderPayment(Long orderId, Payment payment) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        payment.setPaymentDate(LocalDateTime.now());
        order.setPayment(payment);
        order.setUpdateDate(LocalDateTime.now());

        // Update order status based on payment status
        updateOrderStatus(order, payment.getStatus());

        return orderRepository.save(order);
    }

    private void updateOrderStatus(Order order, String paymentStatus) {
        switch (paymentStatus) {
            case "APPROVED":
                order.setStatus("PAID");
                break;
            case "REJECTED":
                order.setStatus("PAYMENT_FAILED");
                break;
            case "PENDING":
                order.setStatus("AWAITING_PAYMENT");
                break;
            default:
                order.setStatus("PAYMENT_PROCESSING");
        }
    }

    public Order cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        validateCancellation(order);

        order.setStatus("CANCELLED");
        order.setUpdateDate(LocalDateTime.now());

        // If payment exists and was approved, trigger refund process
        if (order.getPayment() != null && "APPROVED".equals(order.getPayment().getStatus())) {
            processRefund(order);
        }

        // Return items to stock
        restoreInventory(order);

        return orderRepository.save(order);
    }

    private void validateCancellation(Order order) {
        List<String> nonCancellableStatus = Arrays.asList("DELIVERED", "CANCELLED");
        if (nonCancellableStatus.contains(order.getStatus())) {
            throw new BusinessException("Order cannot be cancelled in current status: " + order.getStatus());
        }
    }

    private void processRefund(Order order) {
        // Implement refund logic here
        order.getPayment().setStatus("REFUNDED");
    }

    private void restoreInventory(Order order) {
        // Implement inventory restoration logic here
    }
}
