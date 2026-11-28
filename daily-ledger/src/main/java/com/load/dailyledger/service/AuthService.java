package com.load.dailyledger.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.load.dailyledger.domain.Users;
import com.load.dailyledger.dto.RegisterForm;
import com.load.dailyledger.dto.UserView;
import com.load.dailyledger.mapper.UserMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    //注册用户
    public UserView register(RegisterForm form){
        // 1. 规范化邮箱
        String email = normalize(form.getEmail());

        // 2. 查重（友好提示）
        Long count = userMapper.selectCount(
            new LambdaQueryWrapper<Users>().eq(Users::getEmail, email));
        if (count != null && count > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "邮箱已注册");
        }

        // 3. 组装实体并加密
        Users user = new Users();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        user.setDisplayName(resolveDisplayName(form.getDisplayName(), email));

        // 4. 唯一索引兜底（并发时查重会漏）
        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "邮箱已注册");
        }

        return new UserView(user.getId(), user.getEmail(), user.getDisplayName());
    }

    //根据邮箱解析显示名
    private String resolveDisplayName(String raw, String email) {
        String name = (raw == null) ? "" : raw.trim();
        if (name.isEmpty()) {
            name = email.substring(0, email.indexOf('@'));
        }
        return name.length() > 50 ? name.substring(0, 50) : name;}

    //根据邮箱查询用户
    public UserView findByEmail(String email) {
        String emailNormalized = normalize(email);
        Users user = userMapper.selectOne(
            new LambdaQueryWrapper<Users>().eq(Users::getEmail, emailNormalized));
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在");
        }
        return UserView.of(user);
    }

    //邮箱规范化
    public static String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
