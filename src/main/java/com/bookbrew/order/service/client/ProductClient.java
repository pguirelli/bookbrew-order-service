package com.bookbrew.order.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.bookbrew.order.service.dto.ProductDTO;

@FeignClient(name = "product-service", url = "${product.service.url}")
public interface ProductClient {

    @GetMapping("/api/products/{id}")
    ProductDTO findProductById(@PathVariable Long id);

    @PutMapping("/api/products/{id}")
    ProductDTO updateProduct(@PathVariable Long id, @RequestBody ProductDTO productDTO);
}
