package com.spartafarmer.agri_commerce.domain.user.entity;

import com.spartafarmer.agri_commerce.common.entity.BaseEntity;
import com.spartafarmer.agri_commerce.common.enums.UserRole;
import com.spartafarmer.agri_commerce.common.exception.CustomException;
import com.spartafarmer.agri_commerce.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    private User(String email, String password, String name, String phone, String address, UserRole role) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.role = role;
    }

    // 회원 생성 시 정적팩토리 사용
    public static User create(String email, String password, String name, String phone, String address, UserRole role) {
        return new User(email, password, name, phone, address, role);
    }

    // 전화번호 포맷팅: DB에 저장할 전화번호 형식 통일 (010-xxxx-xxxx)
    public static String formatPhone(String phone) {

        // 만약을 위한 null 체크
        if (phone == null) return null;

        String formattedPhone = phone.replaceAll("-", "");

        // 방어적으로 전화번호 11자리 체크
        if (formattedPhone.length() != 11) {
            throw new CustomException(ErrorCode.USER_INVALID_PHONE);
        }

        return formattedPhone.substring(0, 3) + "-"
                + formattedPhone.substring(3, 7) + "-"
                + formattedPhone.substring(7);
    }

    // 회원 정보 수정 - 전체 수정(부분 수정 X)
    public void update(String name, String phone, String address) {
        this.name = name;
        this.phone = phone;
        this.address = address;
    }
}
