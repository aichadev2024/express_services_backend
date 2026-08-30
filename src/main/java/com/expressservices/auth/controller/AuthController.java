package com.expressservices.auth.controller;

import com.expressservices.auth.dto.LoginRequest;
import com.expressservices.auth.dto.LoginResponse;
import com.expressservices.auth.dto.RegisterRequest;
import com.expressservices.auth.dto.UserResponse;
import com.expressservices.auth.model.User;
import com.expressservices.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register-livreur")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserResponse> registerLivreur(@RequestBody RegisterRequest request) {
        User user = authService.registerLivreur(request);
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    @GetMapping("/livreurs")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllLivreurs() {
        List<UserResponse> livreurs = authService.getAllLivreurs().stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(livreurs);
    }

    @GetMapping("/has-admin")
    public ResponseEntity<Boolean> hasAdmin() {
        return ResponseEntity.ok(authService.hasAdmin());
    }

    @PostMapping("/register-admin")
    public ResponseEntity<UserResponse> registerAdmin(@RequestBody RegisterRequest request) {
        User user = authService.registerAdmin(request);
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(java.security.Principal principal) {
        User user = authService.getUserEntityByUsername(principal.getName());
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<LoginResponse> verifyOtp(@RequestBody com.expressservices.auth.dto.OtpVerificationRequest request) {
        LoginResponse response = authService.verifyOtp(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<java.util.Map<String, String>> resendOtp(@RequestBody com.expressservices.auth.dto.ResendOtpRequest request) {
        authService.resendOtp(request.getUsername());
        return ResponseEntity.ok(java.util.Map.of("message", "Un nouveau code OTP a été envoyé à votre adresse e-mail."));
    }

    @PostMapping("/profile-photo")
    public ResponseEntity<UserResponse> uploadProfilePhoto(@RequestParam("file") org.springframework.web.multipart.MultipartFile file, java.security.Principal principal) {
        UserResponse response = authService.uploadProfilePhoto(principal.getName(), file);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody com.expressservices.auth.dto.ChangePasswordRequest request, java.security.Principal principal) {
        authService.changePassword(principal.getName(), request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/livreurs/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserResponse> updateLivreur(@PathVariable Long id, @RequestBody com.expressservices.auth.dto.UpdateUserRequest request) {
        User user = authService.updateLivreur(id, request);
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    @DeleteMapping("/livreurs/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        authService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/livreurs/{id}/toggle-active")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserResponse> toggleUserActivation(@PathVariable Long id) {
        User user = authService.toggleUserActivation(id);
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }
}
