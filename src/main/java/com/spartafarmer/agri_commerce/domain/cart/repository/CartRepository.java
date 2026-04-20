package com.spartafarmer.agri_commerce.domain.cart.repository;

import com.spartafarmer.agri_commerce.domain.cart.entity.Cart;
import com.spartafarmer.agri_commerce.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUser(User user);

    @Query("SELECT c FROM Cart c JOIN FETCH c.cartItems ci JOIN FETCH ci.product WHERE c.user = :user")
    Optional<Cart> findByUserWithItems(@Param("user") User user);
}
