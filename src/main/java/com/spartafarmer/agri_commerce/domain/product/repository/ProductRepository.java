package com.spartafarmer.agri_commerce.domain.product.repository;

import com.spartafarmer.agri_commerce.common.enums.ProductStatus;
import com.spartafarmer.agri_commerce.common.enums.ProductType;
import com.spartafarmer.agri_commerce.common.exception.CustomException;
import com.spartafarmer.agri_commerce.common.exception.ErrorCode;
import com.spartafarmer.agri_commerce.domain.product.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // 전체 상품 최신순 조회
    Page<Product> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // 특정 타입의 상품만 최신순으로 조회(특가)
    Page<Product> findByTypeOrderByCreatedAtDesc(ProductType type, Pageable pageable);

    // 특가 상품 중 현재 판매중인 상품만 최신순으로 조회
    Page<Product> findByTypeAndStatusOrderByCreatedAtDesc(
            ProductType type,
            ProductStatus status,
            Pageable pageable
    );

    // 상품명에 keyword가 포함된 상품을 대소문자 구분 없이 최신순으로 조회
    Page<Product> findByNameContainingIgnoreCaseOrderByCreatedAtDesc(String keyword, Pageable pageable);

    // 타임세일 상태 변경 시 비관적 락을 적용하여 상품 조회
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :productId")
    Optional<Product> findByIdWithLock(@Param("productId") Long productId);

    // 공통 조회 메서드
    default Product findByIdOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
    }
}
