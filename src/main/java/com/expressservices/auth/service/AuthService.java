package com.expressservices.auth.service;

import com.expressservices.auth.dto.LoginRequest;
import com.expressservices.auth.dto.LoginResponse;
import com.expressservices.auth.dto.RegisterRequest;
import com.expressservices.auth.dto.UserResponse;
import com.expressservices.auth.model.Role;
import com.expressservices.auth.model.User;
import com.expressservices.auth.repository.UserRepository;
import com.expressservices.exception.ResourceNotFoundException;
import com.expressservices.security.CustomUserDetailsService;
import com.expressservices.security.JwtUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import com.expressservices.email.service.EmailService;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtUtils jwtUtils, AuthenticationManager authenticationManager,
                       CustomUserDetailsService userDetailsService,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.emailService = emailService;
    }

    @PostConstruct
    public void deleteDefaultAdminIfPresent() {
        userRepository.findByUsername("admin").ifPresent(user -> {
            if ("Admin".equals(user.getNom()) && "Système".equals(user.getPrenom()) && Role.ROLE_ADMIN == user.getRole()) {
                userRepository.delete(user);
                System.out.println("====== SYSTEM RESET: Removed default system admin account (username: admin) to allow fresh registration ======");
            }
        });
    }

    private void generateAndSendOtp(User user, String clearTextPassword) {
        java.util.Random random = new java.util.Random();
        String otpCode = String.format("%06d", random.nextInt(1000000));
        user.setOtpCode(otpCode);
        user.setOtpExpiry(java.time.LocalDateTime.now().plusMinutes(10));
        user.setOtpVerified(false);
        user.setFirstLogin(false);

        String recipientName = (user.getPrenom() != null ? user.getPrenom() : "") + " " + (user.getNom() != null ? user.getNom() : "");
        emailService.sendOtpEmail(user.getEmail(), recipientName.trim(), user.getUsername(), clearTextPassword, otpCode);
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        if (!user.isOtpVerified()) {
            if (user.getOtpCode() == null || user.getOtpExpiry() == null || user.getOtpExpiry().isBefore(java.time.LocalDateTime.now())) {
                generateAndSendOtp(user, "[Déjà configuré]");
                userRepository.save(user);
            }
            return LoginResponse.builder()
                    .otpRequired(true)
                    .username(user.getUsername())
                    .role(user.getRole().name())
                    .build();
        }

        String token = jwtUtils.generateToken(userDetails);

        return LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole().name())
                .otpRequired(false)
                .firstLogin(user.isFirstLogin())
                .build();
    }

    @Transactional
    public User registerLivreur(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Ce nom d'utilisateur est déjà pris.");
        }

        boolean hasEmail = request.getEmail() != null && !request.getEmail().trim().isEmpty();

        User livreur = User.builder()
                .username(request.getUsername())
                .email(hasEmail ? request.getEmail().trim() : null)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_LIVREUR)
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .telephone(request.getTelephone())
                .otpVerified(!hasEmail)
                .build();

        User savedLivreur = userRepository.save(livreur);
        if (hasEmail) {
            generateAndSendOtp(savedLivreur, request.getPassword());
        }
        return savedLivreur;
    }

    @Transactional(readOnly = true)
    public List<User> getAllLivreurs() {
        return userRepository.findByRole(Role.ROLE_LIVREUR);
    }

    @Transactional(readOnly = true)
    public boolean hasAdmin() {
        return userRepository.existsByRole(Role.ROLE_ADMIN);
    }

    @Transactional
    public User registerAdmin(RegisterRequest request) {
        if (userRepository.existsByRole(Role.ROLE_ADMIN)) {
            throw new IllegalStateException("L'enregistrement d'administrateur est verrouillé.");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Ce nom d'utilisateur est déjà pris.");
        }

        User admin = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_ADMIN)
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .telephone(request.getTelephone())
                .build();

        User savedAdmin = userRepository.save(admin);
        generateAndSendOtp(savedAdmin, request.getPassword());
        return savedAdmin;
    }

    @Transactional
    public LoginResponse verifyOtp(com.expressservices.auth.dto.OtpVerificationRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        if (user.isOtpVerified()) {
            throw new IllegalArgumentException("Le compte est déjà vérifié.");
        }

        if (user.getOtpExpiry() == null || user.getOtpExpiry().isBefore(java.time.LocalDateTime.now())) {
            throw new IllegalArgumentException("Le code OTP a expiré. Veuillez vous reconnecter pour en générer un nouveau.");
        }

        if (!request.getOtpCode().equals(user.getOtpCode())) {
            throw new IllegalArgumentException("Code OTP incorrect.");
        }

        user.setOtpVerified(true);
        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtUtils.generateToken(userDetails);

        return LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole().name())
                .otpRequired(false)
                .firstLogin(user.isFirstLogin())
                .build();
    }

    @Transactional
    public void resendOtp(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable avec le nom : " + username));

        if (user.isOtpVerified()) {
            throw new IllegalArgumentException("Le compte est déjà vérifié.");
        }

        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Ce compte ne possède pas d'adresse e-mail pour l'envoi de code OTP.");
        }

        generateAndSendOtp(user, "[Mot de passe inchangé]");
        userRepository.save(user);
    }

    @Transactional
    public void changePassword(String username, com.expressservices.auth.dto.ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("L'ancien mot de passe est incorrect.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setFirstLogin(false);
        userRepository.save(user);
    }

    /**
     * Reservee aux autres modules (ex: commande) pour resoudre un utilisateur sans
     * dupliquer l'acces au repository.
     */
    @Transactional(readOnly = true)
    public User getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable avec ID: " + id));
    }

    @Transactional
    public UserResponse uploadProfilePhoto(String username, org.springframework.web.multipart.MultipartFile file) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable: " + username));

        try {
            java.nio.file.Path uploadDir = java.nio.file.Paths.get("uploads", "profiles");
            if (!java.nio.file.Files.exists(uploadDir)) {
                java.nio.file.Files.createDirectories(uploadDir);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            } else {
                extension = ".jpg";
            }

            String filename = "profile_" + user.getId() + "_" + java.util.UUID.randomUUID().toString() + extension;
            java.nio.file.Path filePath = uploadDir.resolve(filename);
            java.nio.file.Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            String photoUrl = "/uploads/profiles/" + filename;
            user.setPhotoUrl(photoUrl);
            User savedUser = userRepository.save(user);

            return UserResponse.fromEntity(savedUser);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Erreur lors de la sauvegarde de la photo de profil : " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public User getUserEntityByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable: " + username));
    }

    @Transactional
    public User updateLivreur(Long id, com.expressservices.auth.dto.UpdateUserRequest request) {
        User user = getUserEntityById(id);
        
        if (request.getNom() != null) user.setNom(request.getNom());
        if (request.getPrenom() != null) user.setPrenom(request.getPrenom());
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail().trim().isEmpty() ? null : request.getEmail().trim());
        }
        if (request.getTelephone() != null) user.setTelephone(request.getTelephone());
        
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = getUserEntityById(id);
        try {
            userRepository.delete(user);
            userRepository.flush(); // Trigger constraints immediately
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Impossible de supprimer ce compte : il est lié à des commandes existantes dans l'historique. Veuillez le détacher des commandes ou le désactiver.");
        }
    }
}
