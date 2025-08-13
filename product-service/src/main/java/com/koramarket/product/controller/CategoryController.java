package com.koramarket.product.controller;

import com.koramarket.product.dto.CategoryRequestDTO;
import com.koramarket.product.dto.CategoryResponseDTO;
import com.koramarket.product.mappper.CategoryMapper;
import com.koramarket.product.model.Category;
import com.koramarket.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryResponseDTO> getAllCategories() {
        return categoryService.findAll().stream()
                .map(CategoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> getCategoryById(@PathVariable Long id) {
        return categoryService.findById(id)
                .map(CategoryMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDTO> createCategory(@RequestBody CategoryRequestDTO dto) {
        Category parent = null;
        if (dto.getParentId() != null) {
            parent = categoryService.findById(dto.getParentId()).orElse(null);
        }
        Category saved = categoryService.save(CategoryMapper.toEntity(dto, parent));
        return ResponseEntity.ok(CategoryMapper.toResponse(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> updateCategory(@PathVariable Long id, @RequestBody CategoryRequestDTO dto) {
        return categoryService.findById(id)
                .map(existing -> {
                    existing.setNom(dto.getNom());
                    existing.setDescription(dto.getDescription());
                    if (dto.getParentId() != null) {
                        Category parent = categoryService.findById(dto.getParentId()).orElse(null);
                        existing.setParent(parent);
                    } else {
                        existing.setParent(null);
                    }
                    Category updated = categoryService.save(existing);
                    return ResponseEntity.ok(CategoryMapper.toResponse(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        if (categoryService.findById(id).isPresent()) {
            categoryService.delete(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Endpoint pour afficher les racines (optionnel)
    @GetMapping("/roots")
    public List<CategoryResponseDTO> getRootCategories() {
        return categoryService.findRootCategories().stream()
                .map(CategoryMapper::toResponse)
                .collect(Collectors.toList());
    }
}
