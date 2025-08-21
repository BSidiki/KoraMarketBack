package com.koramarket.order.client;

import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@FeignClient(name="product-service", url="${clients.product.base-url}")
public interface ProductInventoryClient {

    @PostMapping("/api/products/stock/reserve")
    ReserveStockResponse reserve(@RequestBody ReserveStockRequestDTO req);

    @PostMapping("/api/products/stock/release")
    void release(@RequestBody ReleaseStockRequest req);

    @Data
    class ReserveStockRequestDTO { private UUID orderId; private List<Line> lines;
        @Data public static class Line { private Long productId; private int quantity; } }
    @Data class ReserveStockResponse { private UUID reservationId; private UUID orderId; private String status; private List<Line> lines;
        @Data public static class Line { private Long productId; private int quantity; } }
    @Data class ReleaseStockRequest { private UUID orderId; }
}

