package com.bookbrew.product.service.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bookbrew.product.service.exception.BadRequestException;
import com.bookbrew.product.service.exception.DuplicateNameException;
import com.bookbrew.product.service.exception.ResourceNotFoundException;
import com.bookbrew.product.service.model.Brand;
import com.bookbrew.product.service.repository.BrandRepository;
import com.bookbrew.product.service.repository.ProductRepository;

@Service
public class BrandService {

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<Brand> getAllBrands() {
        List<Brand> brands = brandRepository.findAll();
        if (brands.isEmpty()) {
            throw new ResourceNotFoundException("No brands found");
        }
        return brands;
    }

    public Brand getBrandById(Long id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + id));
    }

    public Brand createBrand(Brand brand) {
        validateBrand(brand);
        return brandRepository.save(brand);
    }

    public Brand updateBrand(Long id, Brand updateBrand) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + id));

        if (updateBrand.getDescription() != null)
            brand.setDescription(updateBrand.getDescription());
        if (updateBrand.getStatus() != null)
            brand.setStatus(updateBrand.getStatus());

        return brandRepository.save(brand);
    }

    public void deleteBrand(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + id));

        if (productRepository.existsByBrand(brand)) {
            throw new BadRequestException(
                    "Cannot delete brand with ID " + id + ". There are products associated with this brand");
        }

        brandRepository.delete(brand);
    }

    private void validateBrand(Brand brand) {
        if (brand.getDescription() == null || brand.getDescription().trim().isEmpty()) {
            throw new BadRequestException("Brand description cannot be empty");
        }
        if (brandRepository.findByDescription(brand.getDescription()).isPresent()) {
            throw new DuplicateNameException("Description '" + brand.getDescription() + "' is already in use");
        }
    }

}
