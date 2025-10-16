package com.gaubong.teddybearshop.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.gaubong.teddybearshop.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
public class GoogleOAuthService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleOAuthService.class);

    @Value("${google.oauth.client-id}")
    private String googleClientId;

    @Autowired
    private UserService userService;

    private GoogleIdTokenVerifier verifier;

    public GoogleIdTokenVerifier getVerifier() {
        if (verifier == null) {
            verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();
        }
        return verifier;
    }

    public User authenticateGoogleUser(String idTokenString) throws GeneralSecurityException, IOException {
        GoogleIdToken idToken = getVerifier().verify(idTokenString);
        
        if (idToken != null) {
            GoogleIdToken.Payload payload = idToken.getPayload();
            
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String googleId = payload.getSubject();
            
            logger.info("Google OAuth - Authenticating user: {}", email);
            
            // Check if user already exists
            User existingUser = userService.findByEmail(email);
            
            if (existingUser != null) {
                // User exists, update Google ID if not set
                if (existingUser.getGoogleId() == null) {
                    existingUser.setGoogleId(googleId);
                    userService.updateUser(existingUser);
                }
                logger.info("Google OAuth - Existing user authenticated: {}", email);
                return existingUser;
            } else {
                // Create new user
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setFullName(name);
                newUser.setGoogleId(googleId);
                
                // Generate unique username from email
                String username = generateUniqueUsername(email);
                newUser.setUsername(username);
                
                // Set a random password (user won't use it for Google OAuth)
                newUser.setPassword("GOOGLE_OAUTH_USER");
                
                User createdUser = userService.createGoogleUser(newUser);
                logger.info("Google OAuth - New user created: {}", email);
                return createdUser;
            }
        } else {
            throw new IllegalArgumentException("Invalid Google ID token");
        }
    }
    
    private String generateUniqueUsername(String email) {
        String baseUsername = email.split("@")[0];
        String username = baseUsername;
        int counter = 1;
        
        while (userService.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }
        
        return username;
    }
}