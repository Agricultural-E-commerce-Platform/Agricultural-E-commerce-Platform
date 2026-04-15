package com.spartafarmer.agri_commerce.common.enums;

public enum UserRole {

    ADMIN, USER;

    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}
