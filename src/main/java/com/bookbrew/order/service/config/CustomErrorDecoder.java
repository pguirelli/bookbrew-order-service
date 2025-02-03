package com.bookbrew.order.service.config;

import com.bookbrew.order.service.exception.BadRequestException;
import com.bookbrew.order.service.exception.ResourceNotFoundException;

import feign.FeignException;
import feign.Response;
import feign.codec.ErrorDecoder;

public class CustomErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        switch (response.status()) {
            case 400:
                return new BadRequestException("Invalid request");
            case 404:
                return new ResourceNotFoundException("Resource not found");
            default:
                return new FeignException.FeignServerException(
                        response.status(),
                        "Error processing request",
                        response.request(),
                        null,
                        null);
        }
    }
}
