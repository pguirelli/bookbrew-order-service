package com.bookbrew.order.service.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bookbrew.order.service.dto.ProductReviewRequestDTO;
import com.bookbrew.order.service.model.ProductReview;
import com.bookbrew.order.service.service.ProductReviewService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/reviews")
public class ProductReviewController {

    @Autowired
    private ProductReviewService productReviewService;

    @GetMapping
    public ResponseEntity<List<ProductReview>> getAllReviews() {
        return ResponseEntity.ok(productReviewService.getAllReviews());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductReview> getReviewById(@PathVariable Long id) {
        return ResponseEntity.ok(productReviewService.getReviewById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ProductReview>> getReviewsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(productReviewService.getReviewsByUser(userId));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductReview>> getReviewsByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(productReviewService.getReviewsByProduct(productId));
    }

    @PostMapping
    public ResponseEntity<ProductReview> createReview(@Valid @RequestBody ProductReviewRequestDTO reviewRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productReviewService.createReview(reviewRequest));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductReview> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody ProductReviewRequestDTO reviewRequest) {
        return ResponseEntity.ok(productReviewService.updateReview(id, reviewRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        productReviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
}
