package com.spartafarmer.agri_commerce.domain.product.entity;

import com.spartafarmer.agri_commerce.common.entity.BaseEntity;
import com.spartafarmer.agri_commerce.common.enums.ProductStatus;
import com.spartafarmer.agri_commerce.common.enums.ProductType;
import com.spartafarmer.agri_commerce.common.exception.CustomException;
import com.spartafarmer.agri_commerce.common.exception.ErrorCode;
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
public class Product extends BaseEntity {

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

    // 특가 시작 시간
    @Column(name = "sale_start_time")
    private LocalDateTime saleStartTime;

    // 특가 종료 시간
    @Column(name = "sale_end_time")
    private LocalDateTime saleEndTime;

    @Version
    private Long version; // 낙관적 락용 버전 필드

    public void prepareSale() {
        this.status = ProductStatus.READY; // 특가 상품을 시작 전 상태로 준비
    }

    public void startSale() {
        // READY 상태일 때만 시작 가능
        if (this.status != ProductStatus.READY) {
            throw new CustomException(ErrorCode.PRODUCT_NOT_ON_SALE);
        }
        if (this.stock == 0) {
            this.status = ProductStatus.SOLD_OUT;
        } else {
            this.status = ProductStatus.ON_SALE;
        }
    }

    public void endSale() {
        // ON_SALE 또는 SOLD_OUT 상태일 때만 종료 가능
        if (this.status != ProductStatus.ON_SALE && this.status != ProductStatus.SOLD_OUT) {
            throw new CustomException(ErrorCode.PRODUCT_NOT_ON_SALE);
        }
        this.status = ProductStatus.SALE_ENDED;
    }


    // 재고 차감
    public void decreaseStock(int quantity) {
        // 먼저 주문 가능한 상태인지 + 재고는 충분한지 통합 검증
        validateOrderable(quantity);

        this.stock -= quantity;

        if (this.stock == 0) {
            this.status = ProductStatus.SOLD_OUT;
        }
    }

    // 상품 상태 체크
    public void validateOrderable(int quantity) {
        if (this.status == ProductStatus.READY) {
            throw new CustomException(ErrorCode.PRODUCT_NOT_ON_SALE);
        }

        if (this.status == ProductStatus.SOLD_OUT) {
            throw new CustomException(ErrorCode.PRODUCT_SOLD_OUT);
        }

        if (this.status == ProductStatus.SALE_ENDED) {
            throw new CustomException(ErrorCode.PRODUCT_SALE_ENDED);
        }

        if (this.stock < quantity) {
            throw new CustomException(ErrorCode.OUT_OF_STOCK);
        }
    }

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
