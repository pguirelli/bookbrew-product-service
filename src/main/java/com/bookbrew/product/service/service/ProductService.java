package com.bookbrew.product.service.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bookbrew.product.service.dto.ProductDTO;
import com.bookbrew.product.service.dto.ProductImageDTO;
import com.bookbrew.product.service.dto.ProductImagesToListDTO;
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
        product.setCategory(categoryService.getCategoryById(product.getCategory().getId()));
        product.setBrand(brandService.getBrandById(product.getBrand().getId()));
        product.setCreationDate(LocalDateTime.now());
        product.setUpdateDate(LocalDateTime.now());
        if (product.getProductImages() != null && !product.getProductImages().isEmpty()) {
            List<ProductImage> processedImages = product.getProductImages().stream()
                    .map(image -> {
                        if (image.getId() != null) {
                            return productImagesRepository.findById(image.getId())
                                    .orElseThrow(() -> new ResourceNotFoundException(
                                            "Product Image not found with id: " + image.getId()));
                        }
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

            // Keep existing images that are not being updated
            List<Long> updatedImageIds = productDTO.getProductImages().stream()
                    .filter(img -> img.getId() != null)
                    .map(ProductImage::getId)
                    .collect(Collectors.toList());

            product.getProductImages().stream()
                    .filter(img -> !updatedImageIds.contains(img.getId()))
                    .forEach(updatedImages::add);

            // Process updated and new images
            for (ProductImage imageDTO : productDTO.getProductImages()) {
                if (imageDTO.getId() != null) {
                    ProductImage existingImage = productImagesRepository.findById(imageDTO.getId())
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Product Image not found with id: " + imageDTO.getId()));
                    existingImage.setPath(imageDTO.getPath());
                    existingImage.setDescription(imageDTO.getDescription());
                    updatedImages.add(existingImage);
                } else {
                    ProductImage newImage = new ProductImage();
                    newImage.setPath(imageDTO.getPath());
                    newImage.setDescription(imageDTO.getDescription());
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
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        productRepository.delete(product);
    }

    public List<ProductImagesToListDTO> findAllProductImages() {
        List<ProductImage> images = productImagesRepository.findAll();
        if (images.isEmpty()) {
            throw new ResourceNotFoundException("No product images found");
        }
        return convertToListDTO(images);
    }

    public ProductImageDTO findByIdProductImage(Long id) {
        ProductImage image = productImagesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product image not found with id: " + id));
        return convertToDTO(image);
    }

    public ProductImageDTO createProductImage(ProductImageDTO productImageDTO) {
        ProductImage productImage = new ProductImage();
        productImage.setDescription(productImageDTO.getDescription());
        productImage.setPath(productImageDTO.getPath());
        productImage.setProduct(findById(productImageDTO.getProduct().getId()));
        productImagesRepository.save(productImage);
        return convertToDTO(productImagesRepository.findById(productImage.getId()).get());
    }

    private ProductImageDTO convertToDTO(ProductImage productImage) {
        ProductImageDTO dto = new ProductImageDTO();
        dto.setId(productImage.getId());
        dto.setDescription(productImage.getDescription());
        dto.setPath(productImage.getPath());
        dto.setProduct(productImage.getProduct());
        return dto;
    }

    private List<ProductImagesToListDTO> convertToListDTO(List<ProductImage> productImage) {
        List<ProductImagesToListDTO> listDTO = new ArrayList<>();
        for (ProductImage image : productImage) {
            ProductImagesToListDTO dto = new ProductImagesToListDTO();
            dto.setId(image.getId());
            dto.setDescription(image.getDescription());
            dto.setPath(image.getPath());
            dto.setIdProduct(image.getProduct().getId());
            listDTO.add(dto);
        }
        return listDTO;
    }

    @Transactional
    public Product updateProductImage(Long productId, Long productImageId, ProductImageDTO productImageDTO) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        ProductImage productImage = productImagesRepository.findById(productImageId)
                .orElseThrow(() -> new ResourceNotFoundException("Product image not found with id: " + productImageId));

        if (productImageDTO.getDescription() != null)
            productImage.setDescription(productImageDTO.getDescription());

        if (productImageDTO.getPath() != null)
            productImage.setPath(productImageDTO.getPath());

        if (productImageDTO.getProduct() != null)
            productImage.setProduct(productImageDTO.getProduct());

        productImagesRepository.save(productImage);

        return findById(product.getId());
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

}
