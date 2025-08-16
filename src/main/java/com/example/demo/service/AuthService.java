package com.example.demo.service;

import com.example.demo.model.AppUser;
import com.example.demo.model.Role;
import com.example.demo.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
public class AuthService {
    private final AppUserRepository repo;
    private final PasswordEncoder encoder;

    public AuthService(AppUserRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    public boolean usernameExists(String username) { return repo.existsByUsername(username); }

    public AppUser createUser(String username, String rawPassword, boolean admin) {
        Set<Role> roles = admin ? Set.of(Role.USER, Role.ADMIN) : Set.of(Role.USER);
        AppUser user = new AppUser(username, encoder.encode(rawPassword), roles);
        return repo.save(user);
    }

    public Optional<AppUser> findByUsername(String username) { return repo.findByUsername(username); }
}

