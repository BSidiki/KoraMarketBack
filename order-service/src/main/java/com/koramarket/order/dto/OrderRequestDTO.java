// dto/OrderRequestDTO.java
package com.koramarket.order.dto;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.util.*;

@Data
public class OrderRequestDTO {
    @NotBlank
    private String currency;
    @NotEmpty
    private List<Item> items;

    @Data
    public static class Item {
        @NotBlank
        private Long productId;
        @NotNull @Min(1)
        private Integer quantity;
    }
}
