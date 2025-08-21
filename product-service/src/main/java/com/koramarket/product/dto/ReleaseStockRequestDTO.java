package com.koramarket.product.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ReleaseStockRequestDTO { @NotNull
private UUID orderId; }

