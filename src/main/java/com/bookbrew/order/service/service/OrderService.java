package com.bookbrew.order.service.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bookbrew.order.service.client.ProductClient;
import com.bookbrew.order.service.dto.OrderRequestDTO;
import com.bookbrew.order.service.dto.ProductDTO;
import com.bookbrew.order.service.exception.InsufficientStockException;
import com.bookbrew.order.service.exception.ResourceNotFoundException;
import com.bookbrew.order.service.model.Order;
import com.bookbrew.order.service.model.OrderItems;
import com.bookbrew.order.service.model.Payment;
import com.bookbrew.order.service.model.Promotion;
import com.bookbrew.order.service.repository.OrderRepository;
import com.bookbrew.order.service.repository.PromotionRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private ProductClient productClient;

    public List<Order> getAllOrders() {
        List<Order> orders = orderRepository.findAll();

        if (orders.isEmpty()) {
            throw new ResourceNotFoundException("No orders found");
        }

        return orders;
    }

    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
    }

    @Transactional
    public Order createOrder(OrderRequestDTO orderRequest) {
        try {
            validateProductsAvailability(orderRequest.getOrderItems());

            Order order = new Order();
            order.setCustomerId(orderRequest.getCustomerId());
            order.setOrderDate(LocalDateTime.now());
            order.setStatus("AGUARDANDO PAGAMENTO");

            List<OrderItems> orderItems = processOrderItems(orderRequest.getOrderItems(), order);
            order.setOrderItems(orderItems);

            BigDecimal subtotal = calculateSubTotal(orderItems);
            Integer totalItems = calculateItemsCount(orderItems);
            BigDecimal orderDiscount = applyPromotions(orderItems);
            order.setSubTotal(subtotal);
            order.setItemCount(totalItems);
            order.setDiscountAmount(orderDiscount);
            order.setAmount(calculateAmount(subtotal, totalItems, orderDiscount));
            order.setPayment(processPayment(orderRequest.getPayment()));
            order.setAddressId(orderRequest.getDeliveryAddress());
            order.setCreationDate(LocalDateTime.now());

            updateProductInventory(orderItems);

            return orderRepository.save(order);
        } catch (Exception e) {
            throw e;
        }
    }

    @Transactional
    public Order updateOrder(Long orderId, OrderRequestDTO orderRequest) {
        try {
            Order existingOrder = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

            if (orderRequest.getDeliveryAddress() != null) {
                existingOrder.setAddressId(orderRequest.getDeliveryAddress());
            }

            if (orderRequest.getOrderItems() != null) {
                List<OrderItems> currentItems = new ArrayList<>(existingOrder.getOrderItems());
                List<OrderItems> updatedItems = new ArrayList<>();

                List<Long> updatedItemIds = orderRequest.getOrderItems().stream()
                        .filter(item -> item.getId() != null)
                        .map(OrderItems::getId)
                        .collect(Collectors.toList());

                currentItems.removeIf(item -> !updatedItemIds.contains(item.getId()));

                for (OrderItems itemDTO : orderRequest.getOrderItems()) {
                    if (itemDTO.getId() != null) {
                        OrderItems existingItem = currentItems.stream()
                                .filter(item -> item.getId().equals(itemDTO.getId()))
                                .findFirst()
                                .orElseThrow(
                                        () -> new RuntimeException("Order item not found with id: " + itemDTO.getId()));

                        existingItem.setQuantity(itemDTO.getQuantity());
                        existingItem.setProductId(itemDTO.getProductId());
                        existingItem.setUpdateDate(LocalDateTime.now());

                        ProductDTO product = productClient.findProductById(itemDTO.getProductId());
                        existingItem.setPrice(product.getPrice());
                        existingItem
                                .setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity())));

                        updatedItems.add(existingItem);
                    } else {
                        OrderItems newItem = processOrderItems(Collections.singletonList(itemDTO), existingOrder)
                                .get(0);
                        updatedItems.add(newItem);
                    }
                }

                existingOrder.getOrderItems().clear();
                existingOrder.getOrderItems().addAll(updatedItems);

                BigDecimal subtotal = calculateSubTotal(updatedItems);
                Integer totalItems = calculateItemsCount(updatedItems);
                BigDecimal orderDiscount = applyPromotions(updatedItems);

                existingOrder.setSubTotal(subtotal);
                existingOrder.setItemCount(totalItems);
                existingOrder.setDiscountAmount(orderDiscount);
                existingOrder.setAmount(calculateAmount(subtotal, totalItems, orderDiscount));
            }

            if (orderRequest.getStatus() != null) {
                existingOrder.setStatus(orderRequest.getStatus());
            }

            if (orderRequest.getPayment() != null) {
                existingOrder.setPayment(processPayment(orderRequest.getPayment()));
            }

            return orderRepository.save(existingOrder);
        } catch (Exception e) {
            throw e;
        }
    }

    public void deleteOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        orderRepository.delete(order);
    }

    private Payment processPayment(Payment paymentOrder) {
        Payment payment = new Payment();
        payment.setPaymentMethod(paymentOrder.getPaymentMethod());
        payment.setStatus("APROVADO");
        payment.setTransactionCode("TRC" + UUID.randomUUID().toString());
        payment.setPaymentDate(LocalDateTime.now());

        return payment;
    }

    private BigDecimal applyPromotions(List<OrderItems> orderItems) {
        List<Promotion> activePromotions = promotionRepository.findByStatus(true);
        BigDecimal totalDiscount = BigDecimal.ZERO;

        for (OrderItems item : orderItems) {
            for (Promotion promotion : activePromotions) {
                if (promotion.getProductId().equals(item.getProductId())) {
                    BigDecimal itemDiscount = calculateItemDiscount(item, promotion);
                    totalDiscount = totalDiscount.add(itemDiscount);

                    BigDecimal discountedPrice = item.getPrice().subtract(itemDiscount);
                    item.setPrice(discountedPrice);
                    item.setTotalPrice(discountedPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
                }
            }
        }

        return totalDiscount;
    }

    private void validateProductsAvailability(List<OrderItems> items) {
        items.forEach(item -> {
            ProductDTO product = productClient.findProductById(item.getProductId());
            if (product.getStock() < item.getQuantity()) {
                throw new InsufficientStockException("Insufficient stock for product: " + product.getTitle());
            }
        });
    }

    private BigDecimal calculateItemDiscount(OrderItems item, Promotion promotion) {
        BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

        BigDecimal discountAmount = itemTotal.multiply(promotion.getDiscountPercentage())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        return discountAmount;
    }

    private List<OrderItems> processOrderItems(List<OrderItems> itemRequests, Order order) {
        List<OrderItems> orderItems = new ArrayList<>();
        List<Promotion> activePromotions = promotionRepository.findByStatus(true);

        for (OrderItems item : itemRequests) {
            ProductDTO product = productClient.findProductById(item.getProductId());

            OrderItems orderItem = new OrderItems();
            orderItem.setOrderId(order);
            orderItem.setProductId(product.getId());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPrice(product.getPrice());
            orderItem.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));

            for (Promotion promotion : activePromotions) {
                if (promotion.getProductId().equals(item.getProductId())) {
                    item.setPrice(product.getPrice());
                    BigDecimal itemDiscount = calculateItemDiscount(item, promotion);
                    BigDecimal discountedPrice = product.getPrice().subtract(itemDiscount);
                    orderItem.setDiscountValue(itemDiscount);
                    orderItem.setTotalPrice(discountedPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
                }
            }
            if (orderItem.getDiscountValue() == null) {
                orderItem.setDiscountValue(BigDecimal.ZERO);
            }
            orderItem.setCreationDate(LocalDateTime.now());
            orderItems.add(orderItem);
        }

        return orderItems;
    }

    private BigDecimal calculateSubTotal(List<OrderItems> orderItems) {
        return orderItems.stream()
                .map(OrderItems::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Integer calculateItemsCount(List<OrderItems> orderItems) {
        return orderItems.stream()
                .mapToInt(OrderItems::getQuantity)
                .sum();
    }

    private Double calculateAmount(BigDecimal subtotal, Integer totalItems, BigDecimal orderDiscount) {
        return subtotal.subtract(orderDiscount != null ? orderDiscount : BigDecimal.ZERO)
                .doubleValue();
    }

    private void updateProductInventory(List<OrderItems> orderItems) {
        orderItems.forEach(item -> {
            ProductDTO product = productClient.findProductById(item.getProductId());
            product.setStock(product.getStock() - item.getQuantity());
            productClient.updateProduct(product.getId(), product);
        });
    }

    public List<Order> getOrdersByCustomer(Long customerId) {
        List<Order> orders = orderRepository.findByCustomerId(customerId);

        if (orders.isEmpty()) {
            throw new ResourceNotFoundException("No orders found for customer: " + customerId);
        }

        return orders;
    }
}
