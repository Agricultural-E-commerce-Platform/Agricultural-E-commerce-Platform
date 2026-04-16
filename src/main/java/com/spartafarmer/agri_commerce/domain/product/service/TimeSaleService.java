package com.spartafarmer.agri_commerce.domain.product.service;

import com.spartafarmer.agri_commerce.common.exception.CustomException;
import com.spartafarmer.agri_commerce.common.exception.ErrorCode;
import com.spartafarmer.agri_commerce.domain.product.entity.Product;
import com.spartafarmer.agri_commerce.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
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

        product.startSale(); // 엔티티 스스로 판매 시작 규칙 처리
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

        product.endSale(); // 엔티티 스스로 판매 종료 규칙 처리
    }

    @Recover
    public void recover(ObjectOptimisticLockingFailureException e, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND)); // 상품이 없으면 예외

        // 이미 다른 트랜잭션이 정상적으로 상태를 바꿨다면 추가 처리 없이 종료
        // 여전히 상태가 기대와 다르면 서버 오류로 처리
        if (product.getStatus() != null) {
            return;
        }

        throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
}