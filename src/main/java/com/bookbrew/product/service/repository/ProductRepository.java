package com.bookbrew.product.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bookbrew.product.service.model.Brand;
import com.bookbrew.product.service.model.Category;
import com.bookbrew.product.service.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByBrand(Brand brand);

    boolean existsByCategory(Category category);

}
