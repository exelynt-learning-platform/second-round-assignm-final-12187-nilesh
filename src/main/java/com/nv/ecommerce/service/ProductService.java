package com.nv.ecommerce.service;

import com.nv.ecommerce.dto.request.ProductRequestDto;
import com.nv.ecommerce.dto.response.ProductResponseDto;
import org.springframework.data.domain.Page;

public interface ProductService {

    ProductResponseDto createProduct(ProductRequestDto request);

    ProductResponseDto getProductById(Long id);

    Page<ProductResponseDto> getAllProducts(int page, int size, String sortBy, String direction);

    ProductResponseDto updateProduct(Long id, ProductRequestDto request);

    void deleteProduct(Long id);
}