package com.bookbrew.product.service.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bookbrew.product.service.dto.ProductDTO;
import com.bookbrew.product.service.dto.ProductImageDTO;
import com.bookbrew.product.service.dto.ProductImagesSearchDTO;
import com.bookbrew.product.service.dto.ProductSearchDTO;
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

    public List<ProductSearchDTO> findAll() {
        List<Product> products = productRepository.findAll();
        List<ProductSearchDTO> productsDTO = new ArrayList<>();

        if (products.isEmpty()) {
            throw new ResourceNotFoundException("No products found");
        }

        for (Product product : products) {
            convertToSearchDTO(product);
            productsDTO.add(convertToSearchDTO(product));
        }

        return productsDTO;
    }

    public ProductSearchDTO findById(Long id) {
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
                    existingImage.setPath(imageDTO.getPath());
                    existingImage.setDescription(imageDTO.getDescription());
                    existingImage.setProduct(product);
                    updatedImages.add(existingImage);
                } else {
                    ProductImage newImage = new ProductImage();
                    newImage.setPath(imageDTO.getPath());
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

    public ProductImagesSearchDTO findByIdProductImage(Long id) {
        return convertToSearchDTO(productImagesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product image not found with id: " + id)));
    }

    public ProductImagesSearchDTO createProductImage(ProductImageDTO productImageDTO) {
        Long productId = productImageDTO.getProduct().getId();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        ProductImage productImage = new ProductImage();
        productImage.setDescription(productImageDTO.getDescription());
        productImage.setPath(productImageDTO.getPath());
        productImage.setProduct(product);
        productImagesRepository.save(productImage);

        return convertToSearchDTO(productImage);
    }

    private ProductImagesSearchDTO convertToSearchDTO(ProductImage productImage) {
        ProductImagesSearchDTO dto = new ProductImagesSearchDTO();
        dto.setId(productImage.getId());
        dto.setDescription(productImage.getDescription());
        dto.setPath(productImage.getPath());
        dto.setIdProduct(productImage.getProduct().getId());

        return dto;
    }

    private List<ProductImagesSearchDTO> convertToListDTO(List<ProductImage> productImage) {
        List<ProductImagesSearchDTO> listDTO = new ArrayList<>();

        for (ProductImage image : productImage) {
            ProductImagesSearchDTO dto = new ProductImagesSearchDTO();
            dto.setId(image.getId());
            dto.setDescription(image.getDescription());
            dto.setPath(image.getPath());
            dto.setIdProduct(image.getProduct().getId());
            listDTO.add(dto);
        }

        return listDTO;
    }

    @Transactional
    public ProductImagesSearchDTO updateProductImage(Long productId, Long productImageId,
            ProductImageDTO productImageDTO) {
        ProductImage productImage = productImagesRepository.findById(productImageId)
                .orElseThrow(() -> new ResourceNotFoundException("Product image not found with id: " + productImageId));

        if (productImageDTO.getDescription() != null)
            productImage.setDescription(productImageDTO.getDescription());

        if (productImageDTO.getPath() != null)
            productImage.setPath(productImageDTO.getPath());

        if (productImageDTO.getProduct() != null)
            productImage.setProduct(productImageDTO.getProduct());

        productImagesRepository.save(productImage);

        return convertToSearchDTO(productImage);
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

    private ProductSearchDTO convertToSearchDTO(Product product) {
        ProductSearchDTO dto = new ProductSearchDTO();
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
        dto.setCreationDate(product.getCreationDate());
        dto.setUpdateDate(product.getUpdateDate());
        dto.setCategoryId(product.getCategory().getId());
        dto.setBrandId(product.getBrand().getId());
        dto.setProductImagesId(
                product.getProductImages().stream().map(ProductImage::getId).collect(Collectors.toList()));

        return dto;
    }
}
