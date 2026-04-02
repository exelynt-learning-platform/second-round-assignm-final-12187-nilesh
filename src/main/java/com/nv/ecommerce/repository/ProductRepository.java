package com.nv.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nv.ecommerce.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long>{

}
