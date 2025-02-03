package com.bookbrew.order.service.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bookbrew.order.service.client.ProductClient;
import com.bookbrew.order.service.dto.ProductReviewRequestDTO;
import com.bookbrew.order.service.exception.ResourceNotFoundException;
import com.bookbrew.order.service.model.ProductReview;
import com.bookbrew.order.service.repository.ProductReviewRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class ProductReviewService {

    @Autowired
    private ProductReviewRepository productReviewRepository;

    @Autowired
    private ProductClient productClient;

    public List<ProductReview> getAllReviews() {
        List<ProductReview> reviews = productReviewRepository.findAll();

        if (reviews.isEmpty()) {
            throw new ResourceNotFoundException("No reviews found");
        }

        return reviews;
    }

    public ProductReview getReviewById(Long id) {
        return productReviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + id));
    }

    public List<ProductReview> getReviewsByProduct(Long productId) {
        return productReviewRepository.findByProductId(productId);
    }

    public ProductReview createReview(ProductReviewRequestDTO reviewRequest) {
        if (productClient.findProductById(reviewRequest.getProductId()) == null) {
            throw new RuntimeException("Product not found with id: " + reviewRequest.getProductId());
        }

        ProductReview review = new ProductReview();
        review.setProductId(reviewRequest.getProductId());
        review.setUserId(reviewRequest.getUserId());
        review.setRating(reviewRequest.getRating());
        review.setComment(reviewRequest.getComment());
        review.setStatus(true);
        review.setCreationDate(LocalDateTime.now());

        return productReviewRepository.save(review);
    }

    public ProductReview updateReview(Long id, ProductReviewRequestDTO reviewRequest) {
        ProductReview existingReview = getReviewById(id);

        if (reviewRequest.getRating() != null)
            existingReview.setRating(reviewRequest.getRating());
        if (reviewRequest.getComment() != null)
            existingReview.setComment(reviewRequest.getComment());
        if (reviewRequest.getStatus() != null)
            existingReview.setStatus(reviewRequest.getStatus());

        existingReview.setUpdateDate(LocalDateTime.now());

        return productReviewRepository.save(existingReview);
    }

    public void deleteReview(Long id) {
        ProductReview review = getReviewById(id);
        productReviewRepository.delete(review);
    }
}
