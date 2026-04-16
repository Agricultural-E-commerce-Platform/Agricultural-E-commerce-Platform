package com.spartafarmer.agri_commerce.domain.product.repository;

import com.spartafarmer.agri_commerce.common.enums.ProductStatus;
import com.spartafarmer.agri_commerce.common.enums.ProductType;
import com.spartafarmer.agri_commerce.domain.product.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // 전체 상품 최신순 조회
    Page<Product> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // 특정 타입의 상품만 최신순으로 조회(특가)
    Page<Product> findByTypeOrderByCreatedAtDesc(ProductType type, Pageable pageable);

    // 상품명에 keyword가 포함된 상품을 최신순으로 조회 (검색 API v1)
    Page<Product> findByNameContainingOrderByCreatedAtDesc(String keyword, Pageable pageable);

    // 특가 상품 중 현재 판매중인 상품만 최신순으로 조회
    Page<Product> findByTypeAndStatusOrderByCreatedAtDesc(
            ProductType type,
            ProductStatus status,
            Pageable pageable
    );

    // Quartz Job에서 상품 상태 변경 시 낙관적 락을 고려하여 조회
    @Lock(LockModeType.OPTIMISTIC)
    Optional<Product> findWithLockById(Long id);
}
