package com.load.dailyledger.dto;

import com.load.dailyledger.domain.Users;

public record UserView(Long id, String email, String displayName){

    //静态工厂方法，控制字段可见性
    public static UserView of(Users user) {
        return new UserView(user.getId(), user.getEmail(), user.getDisplayName());
    }
}
