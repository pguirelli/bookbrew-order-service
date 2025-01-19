package com.bookbrew.order.service.dto;

import java.util.List;

import com.bookbrew.order.service.model.Payment;

import lombok.Data;

@Data
public class OrderRequestDTO {
    private CustomerDTO customer;
    private List<OrderItemRequestDTO> items;
    private Payment payment;
}
