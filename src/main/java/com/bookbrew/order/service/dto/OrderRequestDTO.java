package com.bookbrew.order.service.dto;

import java.util.List;

import com.bookbrew.order.service.model.OrderItems;
import com.bookbrew.order.service.model.Payment;

public class OrderRequestDTO {

    private Long customerId;

    private List<OrderItems> orderItems;

    private String status;

    private Payment payment;

    private Long deliveryAddress;

    private List<Long> promotionIds;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public List<OrderItems> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItems> orderItems) {
        this.orderItems = orderItems;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public Long getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(Long deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public List<Long> getPromotionIds() {
        return promotionIds;
    }

    public void setPromotionIds(List<Long> promotionIds) {
        this.promotionIds = promotionIds;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
