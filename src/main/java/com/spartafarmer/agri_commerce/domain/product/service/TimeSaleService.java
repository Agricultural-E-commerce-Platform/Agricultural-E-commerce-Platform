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
        Product product = productRepository.findByIdWithLock(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND)); // 상품이 없으면 예외

        product.startSale(); // 엔티티 스스로 판매 시작 규칙 처리
    }

    @Transactional
    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    public void endProductSale(Long productId) {
        Product product = productRepository.findByIdWithLock(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND)); // 상품이 없으면 예외

        product.endSale(); // 엔티티 스스로 판매 종료 규칙 처리
    }

    @Recover
    public void recoverStart(ObjectOptimisticLockingFailureException e, Long productId) {
        Product product = productRepository.findByIdOrThrow(productId);

        if (product.getStatus() == ProductStatus.ON_SALE || product.getStatus() == ProductStatus.SOLD_OUT) {
            log.warn("타임세일 시작 재시도 후 이미 상태 반영됨. productId={}", productId);
            return;
        }

        log.error("타임세일 시작 처리 최종 실패. productId={}", productId, e);
        throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    @Recover
    public void recoverEnd(ObjectOptimisticLockingFailureException e, Long productId) {
        Product product = productRepository.findByIdOrThrow(productId);

        if (product.getStatus() == ProductStatus.SALE_ENDED) {
            log.warn("타임세일 종료 재시도 후 이미 상태 반영됨. productId={}", productId);
            return;
        }

        log.error("타임세일 종료 처리 최종 실패. productId={}", productId, e);
        throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
}