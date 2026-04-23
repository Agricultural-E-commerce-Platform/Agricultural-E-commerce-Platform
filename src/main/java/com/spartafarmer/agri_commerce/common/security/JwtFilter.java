package com.spartafarmer.agri_commerce.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spartafarmer.agri_commerce.common.exception.ErrorCode;
import com.spartafarmer.agri_commerce.common.response.ApiResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    private void writeErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                objectMapper.writeValueAsString(ApiResponse.error(status, message))
        );
    }

    private void setAuthentication(String token) {
        // 토큰에서 사용자 정보 추출 후 AuthUser 객체 생성
        AuthUser authUser = new AuthUser(
                jwtUtil.getUserIdFromToken(token),
                jwtUtil.getUserEmailFromToken(token),
                jwtUtil.getUserRoleFromToken(token)
        );
        // SecurityContext에 인증 정보 저장 → 이후 @AuthenticationPrincipal로 꺼낼 수 있음
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(authUser, null, authUser.getAuthorities())
        );
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String url = request.getRequestURI();

        // 인증이 필요 없는 URL은 필터를 건너뜀 (회원가입, 로그인, 상품 조회, 검색, 서버 상태 모니터링)
        if(url.startsWith("/api/auth")
                || url.startsWith("/api/products")
                || url.startsWith("/api/v1/products")
                || url.startsWith("/actuator/health")
        ) {
            filterChain.doFilter(request, response);
            return;
        }

        // Authorization 헤더에서 Bearer 토큰 추출
        String bearerToken = request.getHeader("Authorization");

        // v2 검색은 선택적 인증 (토큰 있으면 인증, 없으면 그냥 통과)
        if(url.startsWith("/api/v2/products")) {
            if (bearerToken != null) {
                String token = jwtUtil.extractToken(bearerToken);
                if (token != null && jwtUtil.validateToken(token)) {
                    setAuthentication(token);
                }
            }
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰이 없는 경우 401을 반환
        if (bearerToken == null) {
            log.info("클라이언트 오류 - statusCode: 401, message: JWT 토큰이 필요합니다.");
            writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, ErrorCode.UNAUTHORIZED.getMessage());
            return;
        }

        // "Bearer " prefix 제거 후 순수 토큰 값 추출
        String token = jwtUtil.extractToken(bearerToken);

        try {
            // 토큰 서명 검증 + 만료 시간 체크
            if (!jwtUtil.validateToken(token)) {
                log.info("클라이언트 오류 - statusCode: 401, message: 잘못된 JWT 토큰입니다.");
                writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, ErrorCode.INVALID_TOKEN.getMessage());
                return;
            }

            setAuthentication(token);

            // 다음 필터로 요청 전달
            filterChain.doFilter(request, response);

        } catch (SecurityException | MalformedJwtException e) {
            log.error("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.", e);
            writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, ErrorCode.INVALID_TOKEN.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token, 만료된 JWT token 입니다.", e);
            writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, ErrorCode.EXPIRED_TOKEN.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.", e);
            writeErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, ErrorCode.UNSUPPORTED_TOKEN.getMessage());
        } catch (Exception e) {
            log.error("Internal server error", e);
            writeErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
        }
    }
}
