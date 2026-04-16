package com.spartafarmer.agri_commerce.domain.product.service;

import com.spartafarmer.agri_commerce.common.enums.ProductStatus;
import com.spartafarmer.agri_commerce.common.exception.CustomException;
import com.spartafarmer.agri_commerce.common.exception.ErrorCode;
import com.spartafarmer.agri_commerce.domain.product.entity.Product;
import com.spartafarmer.agri_commerce.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimeSaleService {

    private final ProductRepository productRepository;

    @Transactional
    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class, // 낙관적 락 충돌 시 재시도
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    public void startProductSale(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND)); // 상품이 없으면 예외

        product.startSale(); // 엔티티 스스로 상태 변경
    }

    @Transactional
    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    public void endProductSale(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND)); // 상품이 없으면 예외

        product.endSale(); // 엔티티 스스로 상태 변경
    }

    @Recover
    public void recoverStart(ObjectOptimisticLockingFailureException e, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND)); // 상품이 없으면 예외

        if (product.getStatus() == ProductStatus.ON_SALE || product.getStatus() == ProductStatus.SOLD_OUT) {
            log.warn("타임세일 시작 재시도 후 이미 상태 반영됨. productId={}", productId); // 다른 트랜잭션이 먼저 반영한 경우
            return;
        }

        throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR); // 기대 상태가 아니면 서버 오류
    }

    @Recover
    public void recoverEnd(ObjectOptimisticLockingFailureException e, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND)); // 상품이 없으면 예외

        if (product.getStatus() == ProductStatus.SALE_ENDED) {
            log.warn("타임세일 종료 재시도 후 이미 상태 반영됨. productId={}", productId); // 다른 트랜잭션이 먼저 반영한 경우
            return;
        }

        throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR); // 기대 상태가 아니면 서버 오류
    }
}