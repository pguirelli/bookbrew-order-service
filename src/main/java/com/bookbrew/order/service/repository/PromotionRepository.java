package com.bookbrew.order.service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bookbrew.order.service.model.Promotion;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    List<Promotion> findByProductId(Long productId);

    List<Promotion> findByStatus(Boolean status);

}
