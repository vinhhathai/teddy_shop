package com.gaubong.teddybearshop.controller;

import com.gaubong.teddybearshop.dto.GoogleOAuthRequest;
import com.gaubong.teddybearshop.dto.JwtResponse;
import com.gaubong.teddybearshop.dto.LoginRequest;
import com.gaubong.teddybearshop.dto.SignupRequest;
import com.gaubong.teddybearshop.entity.User;
import com.gaubong.teddybearshop.security.JwtUtils;
import com.gaubong.teddybearshop.service.GoogleOAuthService;
import com.gaubong.teddybearshop.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserService userService;

    @Autowired
    GoogleOAuthService googleOAuthService;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken((UserDetails) authentication.getPrincipal());

            User userPrincipal = (User) authentication.getPrincipal();
            List<String> roles = userPrincipal.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());

            logger.info("POST /signin - Login successful for user: {}", loginRequest.getUsername());

            return ResponseEntity.ok(new JwtResponse(jwt,
                    userPrincipal.getId(),
                    userPrincipal.getUsername(),
                    userPrincipal.getEmail(),
                    userPrincipal.getFullName(),
                    roles));
        } catch (Exception e) {
            logger.error("POST /signin - Login failed for user: {} - {}", loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body("Error: Invalid username or password!");
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        logger.info("POST /signup - {}", signUpRequest.getEmail());

        try {
            if (userService.existsByUsername(signUpRequest.getUsername())) {
                logger.error("POST /signup - Registration failed: Username already taken - {}",
                        signUpRequest.getEmail());
                return ResponseEntity.badRequest()
                        .body("Error: Username is already taken!");
            }

            if (userService.existsByEmail(signUpRequest.getEmail())) {
                logger.error("POST /signup - Registration failed: Email already in use - {}", signUpRequest.getEmail());
                return ResponseEntity.badRequest()
                        .body("Error: Email is already in use!");
            }

            // Create new user's account
            User user = new User(signUpRequest.getUsername(),
                    signUpRequest.getEmail(),
                    signUpRequest.getPassword(),
                    signUpRequest.getFullName());

            user.setPhoneNumber(signUpRequest.getPhoneNumber());
            user.setAddress(signUpRequest.getAddress());

            userService.createUser(user);

            logger.info("POST /signup - Registration successful for user: {}", signUpRequest.getEmail());
            return ResponseEntity.ok("User registered successfully!");
        } catch (Exception e) {
            logger.error("POST /signup - Registration failed: {} - {}", e.getMessage(), signUpRequest.getEmail());
            return ResponseEntity.badRequest()
                    .body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/google")
    public ResponseEntity<?> authenticateGoogleUser(@Valid @RequestBody GoogleOAuthRequest googleOAuthRequest) {
        logger.info("POST /auth/google - Google OAuth authentication attempt");

        try {
            User user = googleOAuthService.authenticateGoogleUser(googleOAuthRequest.getIdToken());
            
            // Generate JWT token for the authenticated user
            String jwt = jwtUtils.generateJwtToken(user);
            
            List<String> roles = user.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());

            logger.info("POST /auth/google - Google OAuth successful for user: {}", user.getEmail());

            return ResponseEntity.ok(new JwtResponse(jwt,
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getFullName(),
                    roles));
        } catch (Exception e) {
            logger.error("POST /auth/google - Google OAuth failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body("Error: " + e.getMessage());
        }
    }
}