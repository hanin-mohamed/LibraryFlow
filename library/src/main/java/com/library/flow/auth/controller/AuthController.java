package com.library.flow.auth.controller;

import com.library.flow.auth.dto.LoginRequest;
import com.library.flow.auth.dto.LoginResponse;
import com.library.flow.auth.service.AuthService;
import com.library.flow.common.dto.AppResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login and receive JWT")
    public AppResponse<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return AppResponse.ok(authService.login(req));
    }
}
