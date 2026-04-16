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

    @Version
    private Long version; // 낙관적 락용 버전 필드

    // 특가 시작 시간
    @Column(name = "sale_start_time")
    private LocalDateTime saleStartTime;

    // 특가 종료 시간
    @Column(name = "sale_end_time")
    private LocalDateTime saleEndTime;

    public void prepareSale() {
        this.status = ProductStatus.READY; // 특가 시작 전 상태로 준비
    }

    public void startSale() {
        if (this.stock == 0) {
            this.status = ProductStatus.SOLD_OUT; // 재고가 없으면 품절 처리
        } else {
            this.status = ProductStatus.ON_SALE; // 재고가 있으면 판매중으로 변경
        }
    }

    public void endSale() {
        this.status = ProductStatus.SALE_ENDED; // 판매 종료 상태로 변경
    }
}
