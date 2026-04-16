package com.spartafarmer.agri_commerce.domain.product.scheduler;

import com.spartafarmer.agri_commerce.common.enums.ProductStatus;
import com.spartafarmer.agri_commerce.common.exception.CustomException;
import com.spartafarmer.agri_commerce.common.exception.ErrorCode;
import com.spartafarmer.agri_commerce.domain.product.entity.Product;
import com.spartafarmer.agri_commerce.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class TimeSaleStartJob implements Job {

    private final ProductRepository productRepository;

    @Override
    @Transactional
    public void execute(JobExecutionContext context) {
        try {
            Long productId = context.getMergedJobDataMap().getLong("productId"); // 예약할 때 넣어둔 상품 ID

            Product product = productRepository.findWithLockById(productId)
                    .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND)); // 상품이 없으면 공통 예외 처리

            if (product.getStock() == 0) {
                product.changeStatus(ProductStatus.SOLD_OUT); // 시작 시점에 재고가 없으면 품절 처리
            } else {
                product.changeStatus(ProductStatus.ON_SALE); // 시작 시간이 되면 판매중으로 변경
            }
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR); // 낙관적 락 충돌 시 공통 예외 처리
        }
    }
}