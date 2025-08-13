package com.koramarket.product.service;

import com.koramarket.product.model.Category;
import com.koramarket.product.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }

    public Optional<Category> findByNom(String nom) {
        return categoryRepository.findByNom(nom);
    }

    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    public void delete(Long id) {
        // Option : vérifier si catégorie a des sous-catégories ou produits associés avant suppression !
        categoryRepository.deleteById(id);
    }

    public List<Category> findRootCategories() {
        return categoryRepository.findByParentIsNull();
    }
}
