package com.library.flow.auth.service;

import com.library.flow.auth.dto.*;
import com.library.flow.auth.entity.AppUser;
import com.library.flow.auth.model.Role;
import com.library.flow.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserAdminService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;

    @Transactional
    public UUID createUser(CreateUserDTO dto) {
        if (repo.existsByEmailIgnoreCase(dto.email())) {
            throw new IllegalArgumentException("email already exists");
        }
        AppUser u = AppUser.builder()
                .username(dto.username())
                .fullName(dto.fullName())
                .email(dto.email())
                .password(encoder.encode(dto.password()))
                .role(dto.role())
                .build();
        repo.save(u);
        return u.getId();
    }

    @Transactional
    public void updateUser(UUID id, UpdateUserDTO dto) {
        AppUser u = repo.findById(id).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("user"));
        if (dto.fullName() != null) u.setFullName(dto.fullName());
        if (dto.username() != null) u.setUsername(dto.username());
        if (dto.role() != null) u.setRole(dto.role());
    }

    @Transactional
    public void deleteById(UUID id) {
        repo.deleteById(id);
    }


    public Page<UserResponse> getAllUsers(Pageable p) {
        return repo.findAll(p).map(u -> new UserResponse(
                u.getId().toString(), u.getUsername(), u.getFullName(), u.getEmail(), u.getRole().name()
        ));
    }

    public UserResponse getUserById(UUID id) {
        AppUser user = repo.findById(id).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("user"));
        return new UserResponse(user.getId().toString(), user.getUsername(), user.getFullName(), user.getEmail(), user.getRole().name());
    }
}
