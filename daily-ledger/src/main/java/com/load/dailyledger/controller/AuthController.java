package com.load.dailyledger.controller;


import com.load.dailyledger.dto.RegisterForm;
import com.load.dailyledger.dto.RegisterResponse;
import com.load.dailyledger.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of("token", token.getToken(), "headerName", token.getHeaderName());
    }

    @PostMapping("/register")
    public RegisterResponse register(@Valid @RequestBody RegisterForm form) {
        return authService.register(form);
    }

}
