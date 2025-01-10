package com.bookbrew.product.service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bookbrew.product.service.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByDescription(String description);
}
