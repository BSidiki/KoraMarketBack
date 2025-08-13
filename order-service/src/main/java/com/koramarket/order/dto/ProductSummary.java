package com.koramarket.order.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductSummary {
    private Long id;

    @JsonAlias({"nom", "name", "title"})
    private String nom;

    @JsonAlias({"prix", "price", "unitPrice"})
    private BigDecimal prix;

    @JsonAlias({"stock", "quantityAvailable", "qty"})
    private Integer stock;

    @JsonAlias({"statut", "status"})
    private String statut;

    @JsonAlias({"vendeurEmail", "vendorEmail", "sellerEmail"})
    private String vendeurEmail;

    @JsonAlias({"vendeurId", "vendorId", "sellerId"})
    private UUID vendeurId;

    // Références / SKU
    @JsonAlias({"sku", "SKU"})
    private String sku;

    @JsonAlias({"reference", "ref", "code"})
    private String reference;

    // URL d'image "plate"
    @JsonAlias({"imageUrl", "image_url", "image", "thumbnail", "mainImage", "coverUrl"})
    private String imageUrl;

    // Variantes: tableau d'URLs (strings)
    @JsonAlias({"images", "photos", "pictures", "gallery"})
    private List<String> images;

    // Variantes: tableau d'objets { url/href/src, type, isMain, ... }
    @JsonAlias({"imageObjects", "imageList", "media", "assets"})
    private List<ImageResource> imageObjects;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ImageResource {
        @JsonAlias({"url", "href", "src", "link"})
        private String url;

        @JsonAlias({"type", "kind"})
        private String type;

        @JsonAlias({"main", "isMain", "primary"})
        private Boolean main;
    }

    /** Retourne la meilleure URL d'image trouvée */
    public String preferredImage() {
        if (notBlank(imageUrl)) return imageUrl;
        if (images != null) {
            for (String s : images) if (notBlank(s)) return s;
        }
        if (imageObjects != null) {
            // Priorité à main=true, puis type=MAIN, sinon 1ère non vide
            for (ImageResource r : imageObjects) if (r != null && Boolean.TRUE.equals(r.getMain()) && notBlank(r.getUrl())) return r.getUrl();
            for (ImageResource r : imageObjects) if (r != null && "MAIN".equalsIgnoreCase(safe(r.getType())) && notBlank(r.getUrl())) return r.getUrl();
            for (ImageResource r : imageObjects) if (r != null && notBlank(r.getUrl())) return r.getUrl();
        }
        return null;
    }

    /** Retourne la meilleure référence produit trouvée (SKU ou reference) */
    public String preferredSku() {
        if (notBlank(sku)) return sku;
        if (notBlank(reference)) return reference;
        return null;
    }

    private static boolean notBlank(String s) { return s != null && !s.trim().isEmpty(); }
    private static String safe(String s) { return s == null ? "" : s.trim(); }
}
