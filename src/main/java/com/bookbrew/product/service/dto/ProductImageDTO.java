package com.bookbrew.product.service.dto;

import org.springframework.web.multipart.MultipartFile;

import com.bookbrew.product.service.model.Product;

public class ProductImageDTO {

    private Long id;

    private String description;

    private MultipartFile image;

    private Product product;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MultipartFile getImage() {
        return image;
    }

    public void setImage(MultipartFile image) {
        this.image = image;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
