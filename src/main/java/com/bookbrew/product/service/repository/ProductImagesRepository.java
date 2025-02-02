package com.bookbrew.product.service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bookbrew.product.service.model.ProductImage;

@Repository
public interface ProductImagesRepository extends JpaRepository<ProductImage, Long> {

    Optional<ProductImage> findById(Long productImageId);

    List<ProductImage> findByProductId(Long productId);

}
