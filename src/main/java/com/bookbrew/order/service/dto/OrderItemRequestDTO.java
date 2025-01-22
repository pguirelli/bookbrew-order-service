package com.bookbrew.order.service.dto;

import java.math.BigDecimal;
import java.util.List;

public class OrderItemRequestDTO {

    private Long productId;

    private Integer quantity;

    private BigDecimal unitPrice;

    private List<Long> appliedPromotionIds;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public List<Long> getAppliedPromotionIds() {
        return appliedPromotionIds;
    }

    public void setAppliedPromotionIds(List<Long> appliedPromotionIds) {
        this.appliedPromotionIds = appliedPromotionIds;
    }

}