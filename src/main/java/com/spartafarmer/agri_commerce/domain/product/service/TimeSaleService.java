package com.spartafarmer.agri_commerce.domain.product.service;

import com.spartafarmer.agri_commerce.common.enums.ProductStatus;
import com.spartafarmer.agri_commerce.common.exception.CustomException;
import com.spartafarmer.agri_commerce.common.exception.ErrorCode;
import com.spartafarmer.agri_commerce.domain.product.entity.Product;
import com.spartafarmer.agri_commerce.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TimeSaleService {

    private final ProductRepository productRepository;

    @Transactional
    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class, // 낙관적 락 충돌 시 재시도
            maxAttempts = 3,                                          // 최대 3번 재시도
            backoff = @Backoff(delay = 100)                           // 0.1초 대기 후 재시도
    )
    public void startProductSale(Long productId) {
        Product product = productRepository.findWithLockById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND)); // 상품이 없으면 예외

        if (product.getStock() == 0) {
            product.changeStatus(ProductStatus.SOLD_OUT); // 시작 시점에 재고가 없으면 품절 처리
        } else {
            product.changeStatus(ProductStatus.ON_SALE); // 시작 시간이 되면 판매중으로 변경
        }
    }

    @Transactional
    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class, // 낙관적 락 충돌 시 재시도
            maxAttempts = 3,                                          // 최대 3번 재시도
            backoff = @Backoff(delay = 100)                           // 0.1초 대기 후 재시도
    )
    public void endProductSale(Long productId) {
        Product product = productRepository.findWithLockById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND)); // 상품이 없으면 예외

        product.changeStatus(ProductStatus.SALE_ENDED); // 종료 시간이 되면 판매 종료 상태로 변경
    }
}