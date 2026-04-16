package com.spartafarmer.agri_commerce.domain.product.scheduler;

import com.spartafarmer.agri_commerce.common.enums.ProductStatus;
import com.spartafarmer.agri_commerce.domain.product.entity.Product;
import com.spartafarmer.agri_commerce.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class TimeSaleEndJob implements Job {

    private final ProductRepository productRepository;

    @Override
    @Transactional
    public void execute(JobExecutionContext context) {
        Long productId = context.getMergedJobDataMap().getLong("productId"); // 예약할 때 넣어둔 상품 ID

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        product.changeStatus(ProductStatus.SALE_ENDED); // 종료 시간이 되면 판매 종료 상태로 변경
    }
}
