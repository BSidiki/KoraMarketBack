package com.koramarket.order.client;

import com.koramarket.order.dto.ProductSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class ProductClient {
    private final RestTemplate restTemplate;

    @Value("${services.product.base-url:http://product-service}")
    private String productBaseUrl;

    public ProductSummary getById(Long id) {
        String url = productBaseUrl + "/api/products/{id}";
        return restTemplate.getForObject(url, ProductSummary.class, id);
    }
}
