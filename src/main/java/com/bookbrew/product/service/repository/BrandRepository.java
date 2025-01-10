package com.bookbrew.product.service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bookbrew.product.service.model.Brand;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    Optional<Brand> findByDescription(String description);
}
