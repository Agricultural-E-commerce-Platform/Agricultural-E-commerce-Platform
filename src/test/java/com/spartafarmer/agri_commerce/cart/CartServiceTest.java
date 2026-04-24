package com.spartafarmer.agri_commerce.cart;

import com.spartafarmer.agri_commerce.common.enums.ProductStatus;
import com.spartafarmer.agri_commerce.common.enums.ProductType;
import com.spartafarmer.agri_commerce.common.enums.UserRole;
import com.spartafarmer.agri_commerce.common.exception.CustomException;
import com.spartafarmer.agri_commerce.common.exception.ErrorCode;
import com.spartafarmer.agri_commerce.domain.cart.dto.CartAddRequest;
import com.spartafarmer.agri_commerce.domain.cart.dto.CartAddResponse;
import com.spartafarmer.agri_commerce.domain.cart.dto.CartResponse;
import com.spartafarmer.agri_commerce.domain.cart.dto.CartUpdateRequest;
import com.spartafarmer.agri_commerce.domain.cart.dto.CartUpdateResponse;
import com.spartafarmer.agri_commerce.domain.cart.entity.Cart;
import com.spartafarmer.agri_commerce.domain.cart.entity.CartItem;
import com.spartafarmer.agri_commerce.domain.cart.repository.CartItemRepository;
import com.spartafarmer.agri_commerce.domain.cart.repository.CartRepository;
import com.spartafarmer.agri_commerce.domain.cart.service.CartService;
import com.spartafarmer.agri_commerce.domain.product.entity.Product;
import com.spartafarmer.agri_commerce.domain.product.repository.ProductRepository;
import com.spartafarmer.agri_commerce.domain.user.entity.User;
import com.spartafarmer.agri_commerce.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @InjectMocks
    private CartService cartService;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    void 장바구니_상품추가_성공_신규상품() {
        User user = 유저();
        Product product = 상품("사과", 3000L, 2500L, 10, ProductStatus.ON_SALE);
        Cart cart = Cart.create(user);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(productRepository.findById(1L)).willReturn(Optional.of(product));
        given(cartRepository.findByUser(user)).willReturn(Optional.of(cart));
        given(cartItemRepository.findByCartAndProduct(cart, product)).willReturn(Optional.empty());
        given(cartItemRepository.save(any(CartItem.class))).willAnswer(invocation -> invocation.getArgument(0));

        CartAddResponse response = cartService.addCart(1L, new CartAddRequest(1L, 2));

        assertThat(response.productName()).isEqualTo("사과");
        assertThat(response.price()).isEqualTo(2500L);
        assertThat(response.quantity()).isEqualTo(2);
        assertThat(response.totalPrice()).isEqualTo(5000L);
    }

    @Test
    void 장바구니_상품추가_성공_기존상품이면_수량증가() {
        User user = 유저();
        Product product = 상품("사과", 3000L, 2500L, 10, ProductStatus.ON_SALE);
        Cart cart = Cart.create(user);
        CartItem cartItem = CartItem.create(cart, product, product.getSalePrice(), 2);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(productRepository.findById(1L)).willReturn(Optional.of(product));
        given(cartRepository.findByUser(user)).willReturn(Optional.of(cart));
        given(cartItemRepository.findByCartAndProduct(cart, product)).willReturn(Optional.of(cartItem));

        CartAddResponse response = cartService.addCart(1L, new CartAddRequest(1L, 3));

        assertThat(response.quantity()).isEqualTo(5);
        assertThat(cartItem.getQuantity()).isEqualTo(5);
    }

    @Test
    void 장바구니_상품추가_실패_재고초과() {
        User user = 유저();
        Product product = 상품("사과", 3000L, 2500L, 3, ProductStatus.ON_SALE);
        Cart cart = Cart.create(user);
        CartItem cartItem = CartItem.create(cart, product, product.getSalePrice(), 2);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(productRepository.findById(1L)).willReturn(Optional.of(product));
        given(cartRepository.findByUser(user)).willReturn(Optional.of(cart));
        given(cartItemRepository.findByCartAndProduct(cart, product)).willReturn(Optional.of(cartItem));

        assertThatThrownBy(() -> cartService.addCart(1L, new CartAddRequest(1L, 2)))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.OUT_OF_STOCK));
    }

    @Test
    void 장바구니_조회_성공() {
        User user = 유저();
        Product product = 상품("감자", 4000L, 3000L, 20, ProductStatus.ON_SALE);
        Cart cart = Cart.create(user);
        CartItem.create(cart, product, product.getSalePrice(), 3);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(cartRepository.findByUserWithItems(user)).willReturn(Optional.of(cart));

        CartResponse response = cartService.getCart(1L);

        assertThat(response.cartItems()).hasSize(1);
        assertThat(response.cartItems().get(0).productName()).isEqualTo("감자");
        assertThat(response.totalPrice()).isEqualTo(9000L);
        assertThat(response.isMinOrderAmountMet()).isFalse();

        verify(cartRepository).findByUserWithItems(user);
    }

    @Test
    void 장바구니_수량변경_성공() {
        User user = 유저();
        Product product = 상품("고구마", 6000L, 5000L, 10, ProductStatus.ON_SALE);
        Cart cart = Cart.create(user);
        CartItem cartItem = CartItem.create(cart, product, product.getSalePrice(), 2);

        given(cartItemRepository.findByIdAndCart_User_Id(1L, 1L)).willReturn(Optional.of(cartItem));

        CartUpdateResponse response = cartService.updateQuantity(1L, new CartUpdateRequest(4), 1L);

        assertThat(response.quantity()).isEqualTo(4);
        assertThat(response.totalPrice()).isEqualTo(20000L);
        assertThat(cartItem.getQuantity()).isEqualTo(4);
    }

    @Test
    void 장바구니_삭제_성공_소유자검증() {
        User user = 유저();
        Product product = 상품("당근", 3000L, 2500L, 10, ProductStatus.ON_SALE);
        Cart cart = Cart.create(user);
        CartItem cartItem = CartItem.create(cart, product, product.getSalePrice(), 2);

        given(cartItemRepository.findByIdAndCart_User_Id(1L, 1L)).willReturn(Optional.of(cartItem));

        cartService.deleteCartItem(1L, 1L);

        assertThat(cart.getCartItems()).isEmpty();
        verify(cartItemRepository).findByIdAndCart_User_Id(1L, 1L);
    }

    @Test
    void 장바구니_삭제_실패_다른유저상품() {
        given(cartItemRepository.findByIdAndCart_User_Id(1L, 2L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.deleteCartItem(1L, 2L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.CART_ITEM_NOT_FOUND));
    }

    private User 유저() {
        return User.create(
                "test@test.com",
                "Password123",
                "테스트유저",
                User.formatPhone("01012345678"),
                "서울",
                UserRole.USER
        );
    }

    private Product 상품(String name, Long normalPrice, Long salePrice, int stock, ProductStatus status) {
        return Product.create(
                name,
                ProductType.NORMAL,
                normalPrice,
                salePrice,
                null,
                stock,
                status,
                name + ".jpg"
        );
    }
}