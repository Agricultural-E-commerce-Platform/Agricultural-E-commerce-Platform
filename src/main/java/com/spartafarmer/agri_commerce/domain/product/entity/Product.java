package com.spartafarmer.agri_commerce.domain.product.entity;

import com.spartafarmer.agri_commerce.common.enums.ProductStatus;
import com.spartafarmer.agri_commerce.common.enums.ProductType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 상품명
    @Column(nullable = false)
    private String name;

    // 상품 타입
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductType type;

    // 정상가
    @Column(name = "normal_price", nullable = false)
    private Long normalPrice;

    // 판매가 (할인 없으면 nomalPrice 가격입력)
    @Column(name = "sale_price", nullable = false)
    private Long salePrice;

    // 특가 (일반 상품이면 null)
    @Column(name = "special_price")
    private Long specialPrice;

    // 재고
    @Column(nullable = false)
    private Integer stock;

    // 상품 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;

    // 상품 이미지 (이미지 파일 경로 저장)
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    // 생성일시
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 상품 생성 (더미데이터용)
    public static Product create(String name, ProductType type,
                                 Long normalPrice, Long salePrice, Long specialPrice,
                                 Integer stock, ProductStatus status, String imageUrl) {
        Product product = new Product();
        product.name = name;
        product.type = type;
        product.normalPrice = normalPrice;
        product.salePrice = salePrice;
        product.specialPrice = specialPrice;
        product.stock = stock;
        product.status = status;
        product.imageUrl = imageUrl;
        return product;
    }

}
