package com.bookbrew.order.service.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bookbrew.order.service.client.ProductClient;
import com.bookbrew.order.service.dto.ProductDTO;
import com.bookbrew.order.service.exception.ResourceNotFoundException;
import com.bookbrew.order.service.model.Promotion;
import com.bookbrew.order.service.repository.PromotionRepository;

@Service
public class PromotionService {

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private ProductClient productClient;

    public Promotion createPromotion(Promotion promotion) {
        ProductDTO productResponse = productClient.findProductById(promotion.getProductId());

        promotion.setCreationDate(LocalDateTime.now());
        promotion.setProductDTO(productResponse);
        return promotionRepository.save(promotion);
    }

    public List<Promotion> getAllPromotions() {
        return promotionRepository.findAll();
    }

    public Promotion getPromotionById(Long id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with id: " + id));
    }

    public Promotion updatePromotion(Long id, Promotion promotion) {
        Promotion existingPromotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with id: " + id));

        ProductDTO productResponse = productClient.findProductById(existingPromotion.getProductId());
        existingPromotion.setProductDTO(productResponse);

        if (existingPromotion.getDescription() != null)
            existingPromotion.setDescription(promotion.getDescription());
        if (existingPromotion.getProductId() != null)
            existingPromotion.setProductId(promotion.getProductId());
            productResponse = productClient.findProductById(promotion.getProductId());
            existingPromotion.setProductDTO(productResponse);
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
        Promotion promotion = getPromotionById(id);
        promotionRepository.delete(promotion);
    }

    public List<Promotion> getPromotionsByProduct(Long productId) {
        return promotionRepository.findByProductId(productId);
    }
}
