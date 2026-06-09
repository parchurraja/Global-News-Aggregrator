package com.globalpulse.controller;

import com.globalpulse.dto.JwtResponse;
import com.globalpulse.dto.LoginRequest;
import com.globalpulse.dto.MessageResponse;
import com.globalpulse.dto.SignupRequest;
import com.globalpulse.dto.TokenRefreshRequest;
import com.globalpulse.dto.TokenRefreshResponse;
import com.globalpulse.dto.ResetPasswordRequest;
import com.globalpulse.dto.DeleteAccountRequest;
import com.globalpulse.dto.ForgotUsernameRequest;
import com.globalpulse.model.RefreshToken;
import com.globalpulse.model.User;
import com.globalpulse.repository.BookmarkRepository;
import com.globalpulse.repository.UserRepository;
import com.globalpulse.security.jwt.JwtUtils;
import com.globalpulse.security.services.UserDetailsImpl;
import com.globalpulse.service.RefreshTokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BookmarkRepository bookmarkRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        return ResponseEntity.ok(new JwtResponse(jwt,
                refreshToken.getToken(),
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail()));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user's account
        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));

        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshtoken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtUtils.generateTokenFromUsername(user.getUsername());
                    return ResponseEntity.ok(new TokenRefreshResponse(token, requestRefreshToken));
                })
                .orElseThrow(() -> new RuntimeException("Refresh token is not in database!"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null || !user.getUsername().equals(request.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Invalid username or email."));
        }

        user.setPassword(encoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Invalidate refresh tokens since password changed
        refreshTokenService.deleteByUserId(user.getId());

        return ResponseEntity.ok(new MessageResponse("Password has been reset successfully."));
    }

    @PostMapping("/forgot-username")
    public ResponseEntity<?> forgotUsername(@Valid @RequestBody ForgotUsernameRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: No user found with that email."));
        }

        // In a real app, send an email. For this demo, we'll return it securely or just acknowledge.
        // The user specifically asked for an option to retrieve it.
        return ResponseEntity.ok(new MessageResponse("Your username is: " + user.getUsername()));
    }

    @DeleteMapping("/delete-account")
    public ResponseEntity<?> deleteAccount(@Valid @RequestBody DeleteAccountRequest request) {
        User user = userRepository.findByUsername(request.getUsername()).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: User not found."));
        }

        // Verify password
        if (!encoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Invalid credentials."));
        }

        // Clean up relationships
        refreshTokenService.deleteByUserId(user.getId());
        bookmarkRepository.deleteByUserId(user.getId());
        
        userRepository.delete(user);

        return ResponseEntity.ok(new MessageResponse("Account deleted successfully."));
    }
}
