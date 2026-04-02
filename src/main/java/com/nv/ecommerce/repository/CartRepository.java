package com.nv.ecommerce.repository;

import com.nv.ecommerce.entity.Cart;
import com.nv.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUser(User user);
}