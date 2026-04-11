package com.spartafarmer.agri_commerce.domain.user.repository;

import com.spartafarmer.agri_commerce.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {


}
