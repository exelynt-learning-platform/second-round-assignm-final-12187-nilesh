package com.nv.ecommerce.service.impl;

import com.nv.ecommerce.dto.request.ProductRequestDto;
import com.nv.ecommerce.dto.response.ProductResponseDto;
import com.nv.ecommerce.entity.Product;
import com.nv.ecommerce.exception.ResourceNotFoundException;
import com.nv.ecommerce.mapper.ProductMapper;
import com.nv.ecommerce.repository.ProductRepository;
import com.nv.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@EnableMethodSecurity
public class ProductServiceImpl implements ProductService {

	private final ProductRepository productRepository;

	// ADMIN ONLY --- fine grained check for role
	@Override
	@PreAuthorize("hasRole('ADMIN')")
	public ProductResponseDto createProduct(ProductRequestDto request) {

		Product product = ProductMapper.toEntity(request);

		product = productRepository.save(product);

		return ProductMapper.toResponse(product);
	}


	public ProductResponseDto getProductById(Long id) {

		Product product = productRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found"));

		return ProductMapper.toResponse(product);
	}


	public Page<ProductResponseDto> getAllProducts(int page, int size, String sortBy, String direction) {

		if (sortBy == null || sortBy.isEmpty()) {
			sortBy = "id";
		}

		Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

		Pageable pageable = PageRequest.of(page, size, sort);

		Page<Product> productPage = productRepository.findAll(pageable);

		return productPage.map(ProductMapper::toResponse);
	}

	// ADMIN ONLY
	@Override
	@PreAuthorize("hasRole('ADMIN')")
	public ProductResponseDto updateProduct(Long id, ProductRequestDto request) {

		Product product = productRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found"));

		ProductMapper.updateEntity(product, request);

		product = productRepository.save(product);

		return ProductMapper.toResponse(product);
	}

	// ADMIN ONLY
	@Override
	@PreAuthorize("hasRole('ADMIN')")
	public void deleteProduct(Long id) {

		if (!productRepository.existsById(id)) {
			throw new ResourceNotFoundException("Product not found");
		}

		productRepository.deleteById(id);
	}
}