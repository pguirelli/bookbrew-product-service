package com.bookbrew.product.service.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bookbrew.product.service.dto.ProductDTO;
import com.bookbrew.product.service.dto.ProductImageDTO;
import com.bookbrew.product.service.exception.ResourceNotFoundException;
import com.bookbrew.product.service.model.Product;
import com.bookbrew.product.service.model.ProductImage;
import com.bookbrew.product.service.repository.ProductImagesRepository;
import com.bookbrew.product.service.repository.ProductRepository;

import jakarta.transaction.Transactional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImagesRepository productImageRepository;

    public List<Product> findAll() {
        List<Product> products = productRepository.findAll();
        if (products.isEmpty()) {
            throw new ResourceNotFoundException("No products found");
        }
        return products;
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    @Transactional
    public Product createProduct(Product product) {
        product.setCreationDate(LocalDateTime.now());
        product.setUpdateDate(LocalDateTime.now());
        return productRepository.save(product);
    }

    @Transactional
    public Product update(Long id, ProductDTO productDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        if (productDTO.getCode() != null)
            product.setCode(productDTO.getCode());
        if (productDTO.getTitle() != null)
            product.setTitle(productDTO.getTitle());
        if (productDTO.getDescription() != null)
            product.setDescription(productDTO.getDescription());
        if (productDTO.getPrice() != null)
            product.setPrice(productDTO.getPrice());
        if (productDTO.getStock() != null)
            product.setStock(productDTO.getStock());
        if (productDTO.getMinimumStock() != null)
            product.setMinimumStock(productDTO.getMinimumStock());
        if (productDTO.getStatus() != null)
            product.setStatus(productDTO.getStatus());
        if (productDTO.getWeight() != null)
            product.setWeight(productDTO.getWeight());
        if (productDTO.getHeight() != null)
            product.setHeight(productDTO.getHeight());
        if (productDTO.getWidth() != null)
            product.setWidth(productDTO.getWidth());
        if (productDTO.getLength() != null)
            product.setLength(productDTO.getLength());
        if (productDTO.getSalesQuantity() != null)
            product.setSalesQuantity(productDTO.getSalesQuantity());
        if (productDTO.getCategory() != null)
            product.setCategory(productDTO.getCategory());
        if (productDTO.getBrand() != null)
            product.setBrand(productDTO.getBrand());

        if (productDTO.getProductImages() != null && !productDTO.getProductImages().isEmpty()) {
            product.setProductImages(productDTO.getProductImages());
            for (ProductImage productImage : product.getProductImages()) {
                productImageRepository.save(productImage);
            }
        }

        product.setUpdateDate(LocalDateTime.now());

        productRepository.save(product);
        return findById(product.getId());
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        productRepository.delete(product);
    }

    @Transactional
    public Product updateProductImage(Long productId, Long productImageId, ProductImageDTO productImageDTO) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        ProductImage productImage = productImageRepository.findById(productImageId)
                .orElseThrow(() -> new ResourceNotFoundException("Product image not found with id: " + productImageId));

        if (productImageDTO.getDescription() != null)
            productImage.setDescription(productImageDTO.getDescription());

        if (productImageDTO.getPath() != null)
            productImage.setPath(productImageDTO.getPath());

        if (productImageDTO.getProduct() != null)
            productImage.setProduct(productImageDTO.getProduct());

        productImageRepository.save(productImage);

        return findById(product.getId());
    }

    @Transactional
    public void deleteProductImage(Long productId, Long productImageId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        ProductImage productImageToDelete = productImageRepository.findById(productImageId)
                .orElseThrow(() -> new ResourceNotFoundException("Product image not found with id: " + productImageId));

        if (!product.getProductImages().contains(productImageToDelete)) {
            throw new ResourceNotFoundException("Image does not belong to this product");
        }

        product.getProductImages().remove(productImageToDelete);

        productRepository.save(product);
    }

}
