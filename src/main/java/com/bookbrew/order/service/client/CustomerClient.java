package com.bookbrew.order.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.bookbrew.order.service.dto.CustomerDTO;

@FeignClient(name = "customer-service", url = "${customer.service.url}")
public interface CustomerClient {

    @GetMapping("/api/customers/{id}")
    CustomerDTO findCustomerById(@PathVariable Long id);

}
