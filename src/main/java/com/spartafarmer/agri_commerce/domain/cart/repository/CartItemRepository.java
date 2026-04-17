package com.spartafarmer.agri_commerce.domain.cart.repository;

import com.spartafarmer.agri_commerce.domain.cart.entity.Cart;
import com.spartafarmer.agri_commerce.domain.cart.entity.CartItem;
import com.spartafarmer.agri_commerce.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByCart(Cart cart);

    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

    Optional<CartItem> findByIdAndCart_User_Id(Long cartItemId, Long userId);
}