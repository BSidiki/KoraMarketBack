package com.koramarket.product.service;

import com.koramarket.common.enums.ProductStatus;
import com.koramarket.common.exceptions.BusinessException;
import com.koramarket.product.dto.ProductResponseDTO;
import com.koramarket.product.mappper.ProductMapper;
import com.koramarket.product.model.Category;
import com.koramarket.product.model.Product;
import com.koramarket.product.model.ProductImage;
import com.koramarket.product.repository.CategoryRepository;
import com.koramarket.product.repository.ProductImageRepository;
import com.koramarket.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository imageRepo;
    /* =========================
       Helpers sécurité
       ========================= */
    private static Authentication auth() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
    private static String currentEmail() {
        var a = auth();
        return (a != null && a.isAuthenticated() && a.getPrincipal() instanceof String s) ? s : null;
    }
    private static Set<String> authSet() {
        var a = auth();
        if (a == null) return Set.of();
        return a.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(java.util.stream.Collectors.toSet());
    }
    private static boolean has(String authority) { return authSet().contains(authority); }
    private static boolean hasAny(String... authorities) {
        var set = authSet();
        for (var au : authorities) if (set.contains(au)) return true;
        return false;
    }
    private static boolean isAdmin() { return has("ROLE_ADMIN"); }
    private static boolean isVendeur() { return has("ROLE_VENDEUR"); }

    /* =========================
       READS
       ========================= */
    @Transactional(readOnly = true)
    public List<Product> findAll() { return productRepository.findAll(); }

    @Transactional(readOnly = true)
    public Optional<Product> findById(Long id) { return productRepository.findById(id); }

    @Transactional(readOnly = true)
    public List<Product> findByCategory(Long categoryId) { return productRepository.findByCategoryId(categoryId); }

    @Transactional(readOnly = true)
    public List<Product> findByStatus(ProductStatus status) { return productRepository.findByStatut(status); }

    @Transactional(readOnly = true)
    public Optional<Category> findCategory(Long id) { return categoryRepository.findById(id); }

    @Transactional(readOnly = true)
    public List<Product> findByVendeurEmail(String vendeurEmail) { return productRepository.findByVendeurEmail(vendeurEmail); }

    /* =========================
       WRITES
       ========================= */

    // CREATE
    @Transactional
    public Product save(Product product) {
        // Autorisations: ADMIN, VENDEUR, ou permission fine
        if (!(isAdmin() || isVendeur() || has("PRODUCT_CREATE"))) {
            throw new BusinessException("Accès refusé : vous n'avez pas le droit de créer un produit");
        }

        // Forcer l'ownership côté serveur
        String email = currentEmail();
        if (email == null) throw new BusinessException("Authentification requise");
        product.setVendeurEmail(email);

        // Catégorie: si l'objet a une catégorie avec id, s'assurer qu'elle existe
        if (product.getCategory() != null && product.getCategory().getId() != null) {
            Category cat = categoryRepository.findById(product.getCategory().getId())
                    .orElseThrow(() -> new BusinessException("Catégorie introuvable: " + product.getCategory().getId()));
            product.setCategory(cat);
        }

        return productRepository.save(product);
    }

    // UPDATE
    @Transactional
    public Product updateProduct(Long productId, Product patch) {
        Product existing = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("Produit introuvable"));

        String email = currentEmail();
        if (email == null) throw new BusinessException("Authentification requise");

        boolean canAny = isAdmin() || has("PRODUCT_UPDATE_ANY");
        boolean canOwn = isVendeur() || has("PRODUCT_UPDATE_OWN");
        boolean isOwner = existing.getVendeurEmail() != null && existing.getVendeurEmail().equalsIgnoreCase(email);

        if (!canAny) {
            if (!(canOwn && isOwner)) {
                throw new BusinessException("Accès refusé : vous ne pouvez modifier que vos propres produits");
            }
        }

        // Merge "patch" → "existing" (sans écraser par null)
        if (patch.getNom() != null) existing.setNom(patch.getNom());
        if (patch.getDescription() != null) existing.setDescription(patch.getDescription());
        if (patch.getPrix() != null) existing.setPrix(patch.getPrix());
        if (patch.getStatut() != null) existing.setStatut(patch.getStatut());
        if (patch.getStock() != null) existing.setStock(patch.getStock());

        if (patch.getCategory() != null) {
            Long catId = patch.getCategory().getId();
            if (catId != null) {
                Category cat = categoryRepository.findById(catId)
                        .orElseThrow(() -> new BusinessException("Catégorie introuvable: " + catId));
                existing.setCategory(cat);
            } else {
                existing.setCategory(null); // explicite: enlever catégorie si fourni sans id
            }
        }

        return productRepository.save(existing);
    }

    // DELETE
    @Transactional
    public void delete(Long id) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Produit introuvable"));

        String email = currentEmail();
        if (email == null) throw new BusinessException("Authentification requise");

        boolean canAny = isAdmin() || has("PRODUCT_DELETE_ANY");
        boolean canOwn = isVendeur() || has("PRODUCT_DELETE_OWN");
        boolean isOwner = existing.getVendeurEmail() != null && existing.getVendeurEmail().equalsIgnoreCase(email);

        if (!canAny) {
            if (!(canOwn && isOwner)) {
                throw new BusinessException("Accès refusé : vous ne pouvez supprimer que vos propres produits");
            }
        }

        productRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO getOne(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produit introuvable"));

        String defaultImageUrl = imageRepo
                .findFirstByProduct_IdAndIsDefaultTrueOrderByIdAsc(id)
                .or(() -> imageRepo.findFirstByProduct_IdOrderByIdAsc(id))
                .map(img -> img.getUrlImage())
                .orElse(null);

        // Si tu as un champ p.getSku(), utilise-le. Sinon mets null pour l’instant.
        String sku = (hasSku(p) ? p.getSku() : null);

        return ProductMapper.toResponse(p, defaultImageUrl, sku);
    }

    public String preferredImageUrl(Long productId) {
        return imageRepo.findFirstByProduct_IdAndIsDefaultTrueOrderByIdAsc(productId)
                .or(() -> imageRepo.findFirstByProduct_IdOrderByIdAsc(productId))
                .map(ProductImage::getUrlImage)
                .orElse(null);
    }

    public String preferredSku(Product p) {
        String s = p.getSku();
        if (s != null && !s.isBlank()) return s.trim();
        return "PRD-" + p.getId();
    }

    // ---- B) Endpoints de lecture (DTO enrichi) ----
    public List<ProductResponseDTO> findAllResponses() {
        return productRepository.findAll().stream().map(p ->
                com.koramarket.product.mappper.ProductMapper.toResponse(
                        p,
                        preferredImageUrl(p.getId()),
                        preferredSku(p)
                )
        ).toList();
    }

    public Optional<ProductResponseDTO> findResponseById(Long id) {
        return productRepository.findById(id).map(p ->
                com.koramarket.product.mappper.ProductMapper.toResponse(
                        p,
                        preferredImageUrl(p.getId()),
                        preferredSku(p)
                )
        );
    }

    public List<ProductResponseDTO> findByCategoryResponses(Long categoryId) {
        return productRepository.findByCategoryId(categoryId).stream().map(p ->
                com.koramarket.product.mappper.ProductMapper.toResponse(
                        p,
                        preferredImageUrl(p.getId()),
                        preferredSku(p)
                )
        ).toList();
    }

    public List<ProductResponseDTO> findByStatusResponses(ProductStatus s) {
        return productRepository.findByStatut(s).stream().map(p ->
                com.koramarket.product.mappper.ProductMapper.toResponse(
                        p,
                        preferredImageUrl(p.getId()),
                        preferredSku(p)
                )
        ).toList();
    }

    // ---- C) Création/MàJ : penser à remplir vendeurId & vendeurEmail ----
    public Product save(Product entity, java.util.UUID vendeurId, String vendeurEmail) {
        entity.setVendeurId(vendeurId);
        entity.setVendeurEmail(vendeurEmail);
        return productRepository.save(entity);
    }

    // Helper pour éviter l’erreur de compilation si Product n’a pas sku
    private boolean hasSku(Product p) {
        try {
            p.getClass().getDeclaredMethod("getSku");
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

}
