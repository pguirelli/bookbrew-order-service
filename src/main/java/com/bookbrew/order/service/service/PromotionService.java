package com.bookbrew.order.service.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bookbrew.order.service.exception.ResourceNotFoundException;
import com.bookbrew.order.service.model.Promotion;
import com.bookbrew.order.service.repository.PromotionRepository;

@Service
public class PromotionService {

    @Autowired
    private PromotionRepository promotionRepository;

    public Promotion createPromotion(Promotion promotion) {
        promotion.setCreationDate(LocalDateTime.now());
        return promotionRepository.save(promotion);
    }

    public List<Promotion> getAllPromotions() {
        List<Promotion> promotions = promotionRepository.findAll();

        if (promotions.isEmpty()) {
            throw new ResourceNotFoundException("No promotions found");
        }

        return promotions;
    }

    public Promotion getPromotionById(Long id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with id: " + id));
    }

    public Promotion updatePromotion(Long id, Promotion promotion) {
        Promotion existingPromotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with id: " + id));

        if (existingPromotion.getDescription() != null)
            existingPromotion.setDescription(promotion.getDescription());
        if (existingPromotion.getProductId() != null)
            existingPromotion.setProductId(promotion.getProductId());
        if (existingPromotion.getDiscountPercentage() != null)
            existingPromotion.setDiscountPercentage(promotion.getDiscountPercentage());
        if (existingPromotion.getStartDate() != null)
            existingPromotion.setStartDate(promotion.getStartDate());
        if (existingPromotion.getEndDate() != null)
            existingPromotion.setEndDate(promotion.getEndDate());
        if (existingPromotion.getStatus() != null)
            existingPromotion.setStatus(promotion.getStatus());
        existingPromotion.setUpdateDate(LocalDateTime.now());

        return promotionRepository.save(existingPromotion);
    }

    public void deletePromotion(Long id) {
        promotionRepository.delete(getPromotionById(id));
    }

    public List<Promotion> getPromotionsByProduct(Long productId) {
        return promotionRepository.findByProductId(productId);
    }
}
