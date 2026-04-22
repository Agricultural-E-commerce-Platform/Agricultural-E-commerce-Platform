package com.spartafarmer.agri_commerce.product;

import com.spartafarmer.agri_commerce.common.enums.ProductStatus;
import com.spartafarmer.agri_commerce.common.enums.ProductType;
import com.spartafarmer.agri_commerce.common.exception.CustomException;
import com.spartafarmer.agri_commerce.common.exception.ErrorCode;
import com.spartafarmer.agri_commerce.domain.product.entity.Product;
import com.spartafarmer.agri_commerce.domain.product.repository.ProductRepository;
import com.spartafarmer.agri_commerce.domain.product.service.TimeSaleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimeSaleServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private TimeSaleService timeSaleService;

    @Test
    void 타임세일_시작_성공() {
        // given
        Product readyProduct = Product.create(
                "딸기 특가", ProductType.SPECIAL,
                20000L, 20000L, 15000L,
                50, ProductStatus.READY, null
        );

        when(productRepository.findByIdWithLock(1L)).thenReturn(Optional.of(readyProduct));

        // when
        timeSaleService.startProductSale(1L);

        // then
        assertThat(readyProduct.getStatus()).isEqualTo(ProductStatus.ON_SALE);
    }

    @Test
    void 타임세일_시작_성공_재고없음이면품절() {
        // given
        Product zeroStockReadyProduct = Product.create(
                "재고없는 특가", ProductType.SPECIAL,
                20000L, 20000L, 15000L,
                0, ProductStatus.READY, null
        );

        when(productRepository.findByIdWithLock(1L)).thenReturn(Optional.of(zeroStockReadyProduct));

        // when
        timeSaleService.startProductSale(1L);

        // then
        assertThat(zeroStockReadyProduct.getStatus()).isEqualTo(ProductStatus.SOLD_OUT);
    }

    @Test
    void 타임세일_시작_실패_상품없음() {
        // given
        when(productRepository.findByIdWithLock(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> timeSaleService.startProductSale(1L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    void 타임세일_종료_성공() {
        // given
        Product onSaleSpecialProduct = Product.create(
                "한우 특가", ProductType.SPECIAL,
                50000L, 50000L, 30000L,
                20, ProductStatus.ON_SALE, null
        );

        when(productRepository.findByIdWithLock(1L)).thenReturn(Optional.of(onSaleSpecialProduct));

        // when
        timeSaleService.endProductSale(1L);

        // then
        assertThat(onSaleSpecialProduct.getStatus()).isEqualTo(ProductStatus.SALE_ENDED);
    }

    @Test
    void 타임세일_종료_실패_상품없음() {
        // given
        when(productRepository.findByIdWithLock(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> timeSaleService.endProductSale(1L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
    }
}