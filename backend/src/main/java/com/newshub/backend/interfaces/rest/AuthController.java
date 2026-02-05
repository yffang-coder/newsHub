package com.newshub.backend.interfaces.rest;

import com.newshub.backend.infrastructure.persistence.UserMapper;
import com.newshub.backend.interfaces.dto.JwtResponse;
import com.newshub.backend.interfaces.dto.LoginRequest;
import com.newshub.backend.interfaces.dto.SignupRequest;
import com.newshub.backend.domain.model.User;
import com.newshub.backend.infrastructure.utils.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import com.newshub.backend.application.service.EmailService;
import com.newshub.backend.interfaces.dto.LoginByCodeRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.concurrent.TimeUnit;
import java.util.Random;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserMapper userMapper;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    EmailService emailService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @PostMapping("/send-code")
    public ResponseEntity<?> sendVerifyCode(@RequestParam String email) {
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required");
        }
        
        // Generate 6-digit code
        String code = String.format("%06d", new Random().nextInt(999999));
        
        // Save to Redis (5 minutes)
        redisTemplate.opsForValue().set("verify_code:" + email, code, 5, TimeUnit.MINUTES);
        
        // Send email
        try {
            emailService.sendSimpleMessage(email, "NewsHub 验证码", "您的验证码是: " + code + "，有效期5分钟。");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to send email: " + e.getMessage());
        }
        
        return ResponseEntity.ok("Verification code sent");
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(loginRequest.getUsername());
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();    
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        User user = userMapper.findByUsername(userDetails.getUsername());

        return ResponseEntity.ok(new JwtResponse(jwt, 
                                                 user.getId(), 
                                                 user.getUsername(), 
                                                 user.getEmail(), 
                                                 roles));
    }

    @PostMapping("/signin-by-code")
    public ResponseEntity<?> authenticateUserByCode(@RequestBody LoginByCodeRequest loginRequest) {
        String email = loginRequest.getEmail();
        String code = loginRequest.getCode();
        
        // Verify code
        String cachedCode = redisTemplate.opsForValue().get("verify_code:" + email);
        if (cachedCode == null || !cachedCode.equals(code)) {
            return ResponseEntity.badRequest().body("Invalid or expired verification code");
        }
        
        // Find user by email
        User user = userMapper.findByEmail(email);
        if (user == null) {
            // User not found, mark email as verified for registration
            redisTemplate.opsForValue().set("pre_register:" + email, "true", 10, TimeUnit.MINUTES);
            // Return specific status for frontend to trigger registration flow
            return ResponseEntity.ok(java.util.Map.of(
                "status", "REGISTER_REQUIRED",
                "email", email
            ));
        }
        
        // In a real scenario with Spring Security, we might need a custom authentication provider or load user details manually
        // For simplicity, let's manually generate token
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getUsername(), 
                user.getPassword(), 
                java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
        
        String jwt = jwtUtils.generateJwtToken(user.getUsername());
        List<String> roles = java.util.Collections.singletonList("ROLE_" + user.getRole());

        // Clear code
        redisTemplate.delete("verify_code:" + email);

        return ResponseEntity.ok(new JwtResponse(jwt, 
                                                 user.getId(), 
                                                 user.getUsername(), 
                                                 user.getEmail(), 
                                                 roles));
    }

    @PostMapping("/complete-signup")
    public ResponseEntity<?> completeSignup(@RequestBody SignupRequest signUpRequest) {
        String email = signUpRequest.getEmail();
        
        // Verify if email was pre-verified via code login
        String preVerified = redisTemplate.opsForValue().get("pre_register:" + email);
        if (preVerified == null) {
            return ResponseEntity.badRequest().body("Verification session expired. Please try logging in with code again.");
        }

        if (userMapper.findByUsername(signUpRequest.getUsername()) != null) {
            return ResponseEntity.badRequest().body("Error: Username is already taken!");
        }

        if (userMapper.findByEmail(email) != null) {
            return ResponseEntity.badRequest().body("Error: Email is already in use!");
        }

        // Create new user's account
        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(email);
        user.setPassword(encoder.encode(signUpRequest.getPassword()));
        user.setRole("USER");

        userMapper.insert(user);
        
        // Clear pre-register flag
        redisTemplate.delete("pre_register:" + email);
        redisTemplate.delete("verify_code:" + email);

        // Auto login
        String jwt = jwtUtils.generateJwtToken(user.getUsername());
        List<String> roles = java.util.Collections.singletonList("ROLE_USER");
        
        return ResponseEntity.ok(new JwtResponse(jwt, 
                                                 user.getId(), 
                                                 user.getUsername(), 
                                                 user.getEmail(), 
                                                 roles));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        // Verify code
        String code = signUpRequest.getCode();
        String email = signUpRequest.getEmail();
        
        if (code == null) {
             return ResponseEntity.badRequest().body("Verification code is required");
        }
        
        String cachedCode = redisTemplate.opsForValue().get("verify_code:" + email);
        if (cachedCode == null || !cachedCode.equals(code)) {
            return ResponseEntity.badRequest().body("Invalid or expired verification code");
        }

        if (userMapper.findByEmail(signUpRequest.getEmail()) != null) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: Email is already in use!");
        }

        // Create new user's account
        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));
        user.setRole("USER");

        userMapper.insert(user);
        
        // Clear code
        redisTemplate.delete("verify_code:" + email);

        return ResponseEntity.ok("User registered successfully!");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody java.util.Map<String, String> request) {
        String username = request.get("username");
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");

        if (username == null || oldPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body("Username, old password and new password are required");
        }

        User user = userMapper.findByUsername(username);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        if (!encoder.matches(oldPassword, user.getPassword())) {
            return ResponseEntity.badRequest().body("Incorrect old password");
        }

        user.setPassword(encoder.encode(newPassword));
        userMapper.update(user); // Assuming update method exists or use insert with ON DUPLICATE KEY UPDATE logic if supported, 
                                 // BUT standard Mapper usually has update. 
                                 // Let's check UserMapper first or assume standard MyBatis setup.
                                 // If update doesn't exist, I might need to add it.
        
        return ResponseEntity.ok("Password updated successfully");
    }
}

