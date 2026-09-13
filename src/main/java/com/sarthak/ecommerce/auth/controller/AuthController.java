package com.sarthak.ecommerce.auth.controller;

import com.sarthak.ecommerce.auth.dto.LoginRequest;
import com.sarthak.ecommerce.auth.dto.LoginResponse;
import com.sarthak.ecommerce.auth.dto.RegisterRequest;
import com.sarthak.ecommerce.auth.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public String register( @Valid @RequestBody RegisterRequest request) {

        authService.register(request);
        return "User registered successfully";
    }

    @PostMapping("/login")
    public LoginResponse login( @Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        LoginResponse loginResponse = authService.login(request);
        ResponseCookie cookie = ResponseCookie.from("accessToken",loginResponse.token()).httpOnly(true).secure(false).sameSite("Lax").path("/").maxAge(24*660*60).build();
        response.addHeader(
                "Set-Cookie",
                cookie.toString()
        );

        return new LoginResponse(
                null,
                loginResponse.userId(),
                loginResponse.name(),
                loginResponse.role()
        );
    }
}