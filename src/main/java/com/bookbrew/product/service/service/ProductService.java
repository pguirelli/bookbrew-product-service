package com.bookbrew.product.service.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bookbrew.product.service.dto.ProductDTO;
import com.bookbrew.product.service.dto.ProductImageDTO;
import com.bookbrew.product.service.dto.ProductImagesSearchDTO;
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
    private ProductImagesRepository productImagesRepository;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;

    public List<ProductDTO> findAll() {
        List<Product> products = productRepository.findAll();
        List<ProductDTO> productsDTO = new ArrayList<>();

        if (products.isEmpty()) {
            throw new ResourceNotFoundException("No products found");
        }

        for (Product product : products) {
            convertToSearchDTO(product);
            productsDTO.add(convertToSearchDTO(product));
        }

        return productsDTO;
    }

    public ProductDTO findById(Long id) {
        return convertToSearchDTO(productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id)));
    }

    @Transactional
    public Product createProduct(Product product) {
        product.setCategory(categoryService.getCategoryById(product.getCategory().getId()));
        product.setBrand(brandService.getBrandById(product.getBrand().getId()));
        product.setCreationDate(LocalDateTime.now());

        if (product.getProductImages() != null && !product.getProductImages().isEmpty()) {
            List<ProductImage> processedImages = product.getProductImages().stream()
                    .map(image -> {
                        if (image.getId() != null) {
                            ProductImage existingImage = productImagesRepository.findById(image.getId())
                                    .orElseThrow(() -> new ResourceNotFoundException(
                                            "Product Image not found with id: " + image.getId()));
                            existingImage.setProduct(product);
                            return existingImage;
                        }
                        image.setProduct(product);
                        return image;
                    })
                    .collect(Collectors.toList());
            product.setProductImages(processedImages);
        }

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
            product.setCategory(categoryService.getCategoryById(productDTO.getCategory().getId()));
        if (productDTO.getBrand() != null)
            product.setBrand(brandService.getBrandById(productDTO.getBrand().getId()));

        if (productDTO.getProductImages() != null) {
            List<ProductImage> updatedImages = new ArrayList<>();

            List<Long> updatedImageIds = productDTO.getProductImages().stream()
                    .filter(img -> img.getId() != null)
                    .map(ProductImage::getId)
                    .collect(Collectors.toList());

            product.getProductImages().stream()
                    .filter(img -> !updatedImageIds.contains(img.getId()))
                    .forEach(updatedImages::add);

            for (ProductImage imageDTO : productDTO.getProductImages()) {
                if (imageDTO.getId() != null) {
                    ProductImage existingImage = productImagesRepository.findById(imageDTO.getId())
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Product Image not found with id: " + imageDTO.getId()));
                    existingImage.setImageData(imageDTO.getImageData());
                    existingImage.setDescription(imageDTO.getDescription());
                    existingImage.setProduct(product);
                    updatedImages.add(existingImage);
                } else {
                    ProductImage newImage = new ProductImage();
                    newImage.setImageData(imageDTO.getImageData());
                    newImage.setDescription(imageDTO.getDescription());
                    newImage.setProduct(product);
                    updatedImages.add(newImage);
                }
            }

            product.getProductImages().clear();
            product.getProductImages().addAll(updatedImages);
        }

        product.setUpdateDate(LocalDateTime.now());

        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        productRepository.delete(productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id)));
    }

    public List<ProductImagesSearchDTO> findAllProductImages() {
        List<ProductImage> images = productImagesRepository.findAll();
        if (images.isEmpty()) {
            throw new ResourceNotFoundException("No product images found");
        }
        return convertToListDTO(images);
    }

    public byte[] findByIdProductImage(Long id) {
        ProductImage image = productImagesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product image not found with id: " + id));
        return image.getImageData();
    }

    @Transactional
    public ProductImagesSearchDTO createProductImage(ProductImageDTO productImageDTO) {
        try {
            Long productId = productImageDTO.getProduct().getId();
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

            ProductImage productImage = new ProductImage();
            productImage.setDescription(productImageDTO.getDescription());
            productImage.setImageData(productImageDTO.getImage().getBytes());
            productImage.setProduct(product);

            productImagesRepository.save(productImage);
            return convertToSearchDTO(productImage);
        } catch (IOException e) {
            throw new RuntimeException("Error processing image file", e);
        }
    }

    private ProductImagesSearchDTO convertToSearchDTO(ProductImage productImage) {
        ProductImagesSearchDTO dto = new ProductImagesSearchDTO();
        dto.setId(productImage.getId());
        dto.setDescription(productImage.getDescription());
        dto.setIdProduct(productImage.getProduct().getId());
        dto.setImage(productImage.getImageData());
        return dto;
    }

    private List<ProductImagesSearchDTO> convertToListDTO(List<ProductImage> productImages) {
        return productImages.stream()
                .map(this::convertToSearchDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductImagesSearchDTO updateProductImage(Long productId, Long imageId, ProductImageDTO productImageDTO) {
        try {
            ProductImage productImage = productImagesRepository.findById(imageId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product image not found with id: " + imageId));

            if (productImageDTO.getDescription() != null) {
                productImage.setDescription(productImageDTO.getDescription());
            }

            if (productImageDTO.getImage() != null) {
                productImage.setImageData(productImageDTO.getImage().getBytes());
            }

            productImagesRepository.save(productImage);
            return convertToSearchDTO(productImage);
        } catch (IOException e) {
            throw new RuntimeException("Error processing image file", e);
        }
    }

    @Transactional
    public void deleteProductImage(Long productId, Long productImageId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        ProductImage productImageToDelete = productImagesRepository.findById(productImageId)
                .orElseThrow(() -> new ResourceNotFoundException("Product image not found with id: " + productImageId));

        if (!product.getProductImages().contains(productImageToDelete)) {
            throw new ResourceNotFoundException("Image does not belong to this product");
        }

        product.getProductImages().remove(productImageToDelete);

        productRepository.save(product);
    }

    private ProductDTO convertToSearchDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setCode(product.getCode());
        dto.setTitle(product.getTitle());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        dto.setMinimumStock(product.getMinimumStock());
        dto.setStatus(product.getStatus());
        dto.setWeight(product.getWeight());
        dto.setHeight(product.getHeight());
        dto.setWidth(product.getWidth());
        dto.setLength(product.getLength());
        dto.setSalesQuantity(product.getSalesQuantity());
        dto.setUpdateDate(product.getUpdateDate());
        dto.setCategory(product.getCategory());
        dto.setBrand(product.getBrand());
        dto.setProductImages(
                product.getProductImages().stream().collect(Collectors.toList()));

        return dto;
    }
}
