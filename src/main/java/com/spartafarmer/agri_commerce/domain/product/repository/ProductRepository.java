package com.spartafarmer.agri_commerce.domain.product.repository;

import com.spartafarmer.agri_commerce.common.enums.ProductType;
import com.spartafarmer.agri_commerce.domain.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // 전체 상품 최신순 조회
    Page<Product> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // 특정 타입의 상품만 최신순으로 조회(특가)
    Page<Product> findByTypeOrderByCreatedAtDesc(ProductType type, Pageable pageable);

}
