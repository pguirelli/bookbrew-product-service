package com.bookbrew.product.service.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bookbrew.product.service.exception.BadRequestException;
import com.bookbrew.product.service.exception.DuplicateNameException;
import com.bookbrew.product.service.exception.ResourceNotFoundException;
import com.bookbrew.product.service.model.Category;
import com.bookbrew.product.service.repository.CategoryRepository;
import com.bookbrew.product.service.repository.ProductRepository;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<Category> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        if (categories.isEmpty()) {
            throw new ResourceNotFoundException("No categories found");
        }
        return categories;
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    public Category createCategory(Category category) {
        validateCategory(category);
        return categoryRepository.save(category);
    }

    public Category updateCategory(Long id, Category updateCategory) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        if (updateCategory.getDescription() != null)
            category.setDescription(updateCategory.getDescription());
        if (updateCategory.getStatus() != null)
            category.setStatus(updateCategory.getStatus());

        return categoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        if (productRepository.existsByCategory(category)) {
            throw new BadRequestException(
                    "Cannot delete category with ID " + id + ". There are products associated with this category");
        }

        categoryRepository.delete(category);
    }

    private void validateCategory(Category category) {
        if (category.getDescription() == null || category.getDescription().trim().isEmpty()) {
            throw new BadRequestException("Category description cannot be empty");
        }
        if (categoryRepository.findByDescription(category.getDescription()).isPresent()) {
            throw new DuplicateNameException("Description '" + category.getDescription() + "' is already in use");
        }
    }

}
