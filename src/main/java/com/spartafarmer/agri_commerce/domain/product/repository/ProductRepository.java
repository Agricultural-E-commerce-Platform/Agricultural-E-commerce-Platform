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
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // 전체 상품 최신순 조회
    Page<Product> findByStatusNotOrderByCreatedAtDesc(ProductStatus status, Pageable pageable);

    // 특정 타입의 상품만 최신순으로 조회(특가)
    Page<Product> findByTypeAndStatusNotOrderByCreatedAtDesc(ProductType type, ProductStatus status, Pageable pageable);

    // HIDDEN 제외 검색
    Page<Product> findByNameContainingAndStatusNotOrderByCreatedAtDesc(
            String keyword,
            ProductStatus status,
            Pageable pageable
    );

    // 타임세일 상태 변경 시 비관적 락을 적용하여 상품 조회
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :productId")
    Optional<Product> findByIdWithLock(@Param("productId") Long productId);

    // 주문 생성 시 재고를 DB 레벨에서 원자적으로 차감
    @Modifying(flushAutomatically = true)
    @Query("""
    update Product p
    set p.stock = p.stock - :quantity,
        p.status = case
            when p.stock - :quantity = 0 then :soldOutStatus
            else p.status
        end,
        p.version = p.version + 1
    where p.id = :productId
      and p.status = :onSaleStatus
      and p.stock >= :quantity
""")
    int decreaseStockAtomic(
            @Param("productId") Long productId,
            @Param("quantity") int quantity,
            @Param("onSaleStatus") ProductStatus onSaleStatus,
            @Param("soldOutStatus") ProductStatus soldOutStatus
    );
}
