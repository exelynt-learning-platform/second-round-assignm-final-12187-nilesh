package com.nv.ecommerce.repository;

import com.nv.ecommerce.entity.CartItem;
import com.nv.ecommerce.entity.Product;
import com.nv.ecommerce.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
}