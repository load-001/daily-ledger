package com.load.dailyledger.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.load.dailyledger.domain.Users;
import com.load.dailyledger.mapper.UserMapper;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class DbUserDetailsService implements UserDetailsService {
    private final UserMapper userMapper;

    public DbUserDetailsService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String email = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);

        Users user = userMapper.selectOne(
            new LambdaQueryWrapper<Users>().eq(Users::getEmail, email));

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + email);
        }

        return User.withUsername(user.getEmail())
            .password(user.getPasswordHash())
            .roles("USER")
            .build();
    }
}
