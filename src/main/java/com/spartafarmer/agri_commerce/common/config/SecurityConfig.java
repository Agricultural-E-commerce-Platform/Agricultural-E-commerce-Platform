package com.spartafarmer.agri_commerce.common.config;

import com.spartafarmer.agri_commerce.common.security.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true) // @Secured 어노테이션 활성화
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)  // REST API는 CSRF 불필요
                .httpBasic(AbstractHttpConfigurer::disable) // HTTP Basic 인증 비활성화
                .formLogin(AbstractHttpConfigurer::disable) // 폼 로그인 비활성화 (JWT 사용)
                .addFilterBefore(jwtFilter, SecurityContextHolderAwareRequestFilter.class) // JWT 필터 등록
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()         // 회원가입, 로그인
                        .requestMatchers("/api/products/**").permitAll()     // 상품 조회
                        .requestMatchers("/api/v1/products/**").permitAll()  // 검색 v1
                        .requestMatchers("/api/v2/products/**").permitAll()  // 검색 v2
                        .requestMatchers("/actuator/health").permitAll()  // 서버 상태 모니터링
                        .anyRequest().authenticated() // 나머지는 인증 필요
                )
                .build();
    }

    // 비밀번호 BCrypt 암호화
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
