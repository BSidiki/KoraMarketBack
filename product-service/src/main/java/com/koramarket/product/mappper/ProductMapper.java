package com.koramarket.product.mappper;

import com.koramarket.common.enums.ProductStatus;
import com.koramarket.product.dto.ProductRequestDTO;
import com.koramarket.product.dto.ProductResponseDTO;
import com.koramarket.product.model.Category;
import com.koramarket.product.model.Product;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

@UtilityClass
public class ProductMapper {

    public static Product toEntity(ProductRequestDTO dto, Category category) {
        return Product.builder()
                .nom(dto.getNom())
                .description(dto.getDescription())
                .prix(dto.getPrix())
                .stock(dto.getStock())
                .statut(dto.getStatut() != null
                        ? ProductStatus.valueOf(dto.getStatut())
                        : ProductStatus.DISPONIBLE)
                .category(category)
                .build();
    }

    /** Compat : mapping “simple” (pas d’image/sku enrichis) */
    public static ProductResponseDTO toResponse(Product product) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(product.getId());
        dto.setNom(product.getNom());
        dto.setDescription(product.getDescription());
        dto.setPrix(product.getPrix());
        dto.setStock(product.getStock());
//        dto.setImageUrl(defaultImageUrl);
        dto.setSku(product.getSku());         // ou null si pas de champ
        dto.setVendeurEmail(product.getVendeurEmail());
        dto.setVendeurId(product.getVendeurId());
        dto.setStatut(product.getStatut() != null ? product.getStatut().name() : null);
        dto.setCategoryId(product.getCategory() != null ? product.getCategory().getId() : null);
        dto.setCategoryNom(product.getCategory() != null ? product.getCategory().getNom() : null);
        dto.setDateCreation(product.getDateCreation());
        // imageUrl/sku non fournis ici

        String prixAffiche = fmtUnits(product.getPrix());       // "275 000"
        dto.setPrixAffiche(prixAffiche);
        dto.setPrixAfficheXof(prixAffiche + " XOF");
        return dto;
    }

    /** Mapping enrichi (image par défaut + sku) pour l’endpoint GET /api/products/{id} */
    public static ProductResponseDTO toResponse(Product product, String imageUrl, String sku) {
        ProductResponseDTO dto = toResponse(product);
        dto.setImageUrl(imageUrl);
        dto.setSku(sku);
        return dto;
    }

    private static String fmtUnits(BigDecimal amount) {
        if (amount == null) return "0";
        BigDecimal rounded = amount.setScale(0, BigDecimal.ROUND_HALF_UP); // ✅ compatible partout
        DecimalFormatSymbols sym = new DecimalFormatSymbols(Locale.FRANCE);
        sym.setGroupingSeparator('\u202F'); // espace fine insécable
        DecimalFormat df = new DecimalFormat("#,##0", sym);
        return df.format(rounded);
    }
}
