package com.spartafarmer.agri_commerce.domain.user.entity;

import com.spartafarmer.agri_commerce.common.entity.BaseEntity;
import com.spartafarmer.agri_commerce.common.enums.UserRole;
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

    // 정책문제 - Todo 전화번호도 중복 불가한지 논의해야함(유니크 제약)
    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    // 회원정보 변경 기능이 추가 되어서 updatedAt 만들었음 - Todo 각 엔티티에 모두 필요하면 BaseEntity를 수정하면 되는데 대부분 없어서 여기는 직접 적음
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

    // 회원 정보 수정 - Todo 이름, 주소, 전화번호 모두 필수로 받아서 전체 업데이트 vs 부분 수정 가능
    public void update(String name, String phone, String address) {
        this.name = name;
        this.phone = phone;
        this.address = address;
    }
}
