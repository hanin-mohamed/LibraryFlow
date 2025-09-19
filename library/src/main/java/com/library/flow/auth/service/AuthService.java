package com.library.flow.auth.service;

import com.library.flow.auth.dto.LoginRequest;
import com.library.flow.auth.dto.LoginResponse;
import com.library.flow.auth.entity.AppUser;
import com.library.flow.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authManager;
    private final JwtService jwt;
    private final UserRepository users;

    public LoginResponse login(LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password())
        );

        UserDetails principal = (UserDetails) auth.getPrincipal();
        AppUser user = users.findByEmail(principal.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        String token = jwt.generate(
                user.getEmail(),
                Map.of("uid", user.getId().toString(),
                        "role", user.getRole().name())
        );

        return new LoginResponse(token, user.getRole().name());
    }
}
