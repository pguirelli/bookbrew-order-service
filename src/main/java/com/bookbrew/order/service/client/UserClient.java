package com.bookbrew.order.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.bookbrew.order.service.dto.UserDTO;

@FeignClient(name = "auth-service", url = "${auth.service.url}")
public interface UserClient {

    @GetMapping("/api/users/{userId}")
    UserDTO findUserById(@PathVariable Long id);
}
