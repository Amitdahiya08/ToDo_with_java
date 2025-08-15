package com.example.demo.controller;

import com.example.demo.model.AppUser;
import com.example.demo.model.Role;
import com.example.demo.repository.AppUserRepository;
import com.example.demo.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthController(AppUserRepository userRepository, PasswordEncoder passwordEncoder,
                          AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        boolean admin = Boolean.parseBoolean(body.getOrDefault("admin", "false"));
        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "username and password required"));
        }
        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.badRequest().body(Map.of("error", "username already exists"));
        }
        Set<Role> roles = admin ? Set.of(Role.USER, Role.ADMIN) : Set.of(Role.USER);
        AppUser user = new AppUser(username, passwordEncoder.encode(password), roles);
        userRepository.save(user);
        String token = jwtUtil.generateToken(user.getUsername(), roles.stream().map(Enum::name).collect(java.util.stream.Collectors.toSet()));
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        if (auth.isAuthenticated()) {
            AppUser user = userRepository.findByUsername(username).orElseThrow();
            String token = jwtUtil.generateToken(username, user.getRoles().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet()));
            return ResponseEntity.ok(Map.of("token", token));
        }
        return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
    }
}

